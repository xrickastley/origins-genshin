package io.github.xrickastley.originsgenshin.util;

import de.javagl.obj.FloatTuple;
import de.javagl.obj.Mtl;
import de.javagl.obj.MtlReader;
import de.javagl.obj.Obj;
import de.javagl.obj.ObjReader;
import de.javagl.obj.ObjUtils;

import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.RotationAxis;

import org.joml.Matrix3f;
import org.joml.Matrix4f;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ObjRenderer {
	private Obj obj;
	private Map<String, Mtl> materials;
	private boolean isLoaded = false;

	public ObjRenderer() {
		this.materials = new HashMap<>();
	}

	/**
	 * Loads an OBJ file from an InputStream
	 * @param objStream InputStream for the .obj file
	 * @param mtlStream InputStream for the .mtl file (can be null)
	 * @throws Exception if loading fails
	 */
	public void loadObj(InputStream objStream, InputStream mtlStream) throws IOException {
		// Load the OBJ file
		Obj rawObj = ObjReader.read(objStream);
		
		// Convert to renderable format (triangulated with normals and texture coordinates)
		this.obj = ObjUtils.convertToRenderable(rawObj);
		
		// Load materials if MTL stream is provided
		if (mtlStream != null) {
			List<Mtl> mtlList = MtlReader.read(mtlStream);

			for (Mtl mtl : mtlList) materials.put(mtl.getName(), mtl);
		}
		
		this.isLoaded = true;
	}

	/**
	 * Renders the OBJ model into the world
	 * @param matrices The matrix stack for transformations
	 * @param vertexConsumer The vertex consumer to write vertices to
	 * @param light The light value (packed light coordinates)
	 * @param overlay The overlay value for effects
	 * @param red Red color component (0.0-1.0)
	 * @param green Green color component (0.0-1.0)
	 * @param blue Blue color component (0.0-1.0)
	 * @param alpha Alpha component (0.0-1.0)
	 */
	public void render(MatrixStack matrices, VertexConsumer vertexConsumer, int light, int overlay, float red, float green, float blue, float alpha) {
		if (!isLoaded || obj == null) {
			return;
		}

		Matrix4f positionMatrix = matrices.peek().getPositionMatrix();
		Matrix3f normalMatrix = matrices.peek().getNormalMatrix();

		int numFaces = obj.getNumFaces();

		// Process each face (triangle)
		for (int faceIndex = 0; faceIndex < numFaces; faceIndex++) {
			// Get material for this face group (simplified - using first material or default)
			String materialName = this.getMaterialForFace(faceIndex);
			float[] materialColor = this.getMaterialColor(materialName, red, green, blue);

			// Each face has 3 vertices (since we triangulated)
			for (int vertexInFace = 0; vertexInFace < 3; vertexInFace++) {
				int vertexIndex = obj.getFace(faceIndex).getVertexIndex(vertexInFace);
				int normalIndex = obj.getFace(faceIndex).getNormalIndex(vertexInFace);
				int texCoordIndex = obj.getFace(faceIndex).getTexCoordIndex(vertexInFace);

				// Get vertex position
				float x = obj.getVertex(vertexIndex).getX();
				float y = obj.getVertex(vertexIndex).getY();
				float z = obj.getVertex(vertexIndex).getZ();

				// Get normal (default to up if not available)
				float nx = 0.0f, ny = 1.0f, nz = 0.0f;
				if (normalIndex >= 0 && normalIndex < obj.getNumNormals()) {
					nx = obj.getNormal(normalIndex).getX();
					ny = obj.getNormal(normalIndex).getY();
					nz = obj.getNormal(normalIndex).getZ();
				}

				// Get texture coordinates (default to 0,0 if not available)
				float u = 0.0f, v = 0.0f;
				if (texCoordIndex >= 0 && texCoordIndex < obj.getNumTexCoords()) {
					u = obj.getTexCoord(texCoordIndex).getX();
					v = 1.0f - obj.getTexCoord(texCoordIndex).getY(); // Flip V coordinate for Minecraft
				}

				// Add vertex to the buffer
				vertexConsumer
					.vertex(positionMatrix, x, y, z)
					.color(materialColor[0], materialColor[1], materialColor[2], alpha)
					.texture(u, v)
					.overlay(overlay)
					.light(light)
					.normal(normalMatrix, nx, ny, nz)
					.next();
			}
		}
	}

	/**
	 * Overloaded render method with default white color
	 */
	public void render(MatrixStack matrices, VertexConsumer vertexConsumer, int light, int overlay) {
		render(matrices, vertexConsumer, light, overlay, 1.0f, 1.0f, 1.0f, 1.0f);
	}

	/**
	 * Gets the material name for a specific face
	 * This is a simplified implementation - in practice, you might want to track material groups
	 */
	private String getMaterialForFace(int faceIndex) {
		// For now, return the first available material or null
		if (!materials.isEmpty()) {
			return materials.keySet().iterator().next();
		}
		return null;
	}

	/**
	 * Gets the color for a material, with fallback to provided defaults
	 */
	private float[] getMaterialColor(String materialName, float defaultR, float defaultG, float defaultB) {
		if (materialName != null && materials.containsKey(materialName)) {
			Mtl material = materials.get(materialName);
			
			// Try to get diffuse color (Kd)
			FloatTuple kd = material.getKd();
			if (kd != null && kd.getDimensions() >= 3)
				return new float[]{kd.getX(), kd.getY(), kd.getZ()};
			
			// Fallback to ambient color (Ka)
			FloatTuple ka = material.getKa();
			if (ka != null && ka.getDimensions() >= 3)
				return new float[]{ka.getX(), ka.getY(), ka.getZ()};
		}
		
		// Return default color
		return new float[]{defaultR, defaultG, defaultB};
	}

	/**
	 * Scales the model by the given factor
	 */
	public void scale(MatrixStack matrices, float scale) {
		matrices.scale(scale, scale, scale);
	}

	/**
	 * Rotates the model
	 */
	public void rotate(MatrixStack matrices, float angleX, float angleY, float angleZ) {
		matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(angleX));
		matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(angleY));
		matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(angleZ));
	}

	/**
	 * Translates the model
	 */
	public void translate(MatrixStack matrices, float x, float y, float z) {
		matrices.translate(x, y, z);
	}

	/**
	 * Returns whether the OBJ is loaded and ready to render
	 */
	public boolean isLoaded() {
		return isLoaded;
	}

	/**
	 * Gets the bounding box information of the loaded model
	 */
	public float[] getBounds() {
		if (!isLoaded || obj == null) {
			return new float[]{0, 0, 0, 0, 0, 0}; // minX, minY, minZ, maxX, maxY, maxZ
		}

		float minX = Float.MAX_VALUE, minY = Float.MAX_VALUE, minZ = Float.MAX_VALUE;
		float maxX = Float.MIN_VALUE, maxY = Float.MIN_VALUE, maxZ = Float.MIN_VALUE;

		for (int i = 0; i < obj.getNumVertices(); i++) {
			float x = obj.getVertex(i).getX();
			float y = obj.getVertex(i).getY();
			float z = obj.getVertex(i).getZ();

			minX = Math.min(minX, x);
			minY = Math.min(minY, y);
			minZ = Math.min(minZ, z);
			maxX = Math.max(maxX, x);
			maxY = Math.max(maxY, y);
			maxZ = Math.max(maxZ, z);
		}

		return new float[]{minX, minY, minZ, maxX, maxY, maxZ};
	}

	/**
	 * Centers the model around the origin
	 */
	public void center(MatrixStack matrices) {
		final float[] bounds = getBounds();
		final float centerX = (bounds[0] + bounds[3]) / 2.0f;
		final float centerY = (bounds[1] + bounds[4]) / 2.0f;
		final float centerZ = (bounds[2] + bounds[5]) / 2.0f;
		
		matrices.translate(-centerX, -centerY, -centerZ);
	}

	/**
	 * Gets the number of vertices in the loaded model
	 */
	public int getVertexCount() {
		return isLoaded && obj != null ? obj.getNumVertices() : 0;
	}

	/**
	 * Gets the number of faces in the loaded model
	 */
	public int getFaceCount() {
		return isLoaded && obj != null ? obj.getNumFaces() : 0;
	}
}