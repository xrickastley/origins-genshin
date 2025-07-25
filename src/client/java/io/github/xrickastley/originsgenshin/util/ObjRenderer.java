package io.github.xrickastley.originsgenshin.util;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;

import com.mojang.blaze3d.systems.RenderSystem;

import de.javagl.obj.FloatTuple;
import de.javagl.obj.FloatTuples;
import de.javagl.obj.Mtl;
import de.javagl.obj.MtlReader;
import de.javagl.obj.Obj;
import de.javagl.obj.ObjFace;
import de.javagl.obj.ObjReader;
import de.javagl.obj.ObjSplitting;
import de.javagl.obj.ObjUtils;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.VertexBuffer;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.VertexFormat.DrawMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;

public class ObjRenderer {
	private final Identifier modelId;
	private final Map<String, Mtl> mtlMap = new HashMap<>();
	private Map<String, Obj> mtlObjMap;
	private final Map<Obj, VertexBuffer> objBufferMap = new HashMap<>();
	private boolean baked;

	public ObjRenderer(final Identifier modelId) throws IOException {
		this.modelId = modelId;

		this.load();
	}

	public void render(final MatrixStack matrixStack) {
		if (!this.baked) this.bake();

		final Matrix4f positionMatrix = matrixStack.peek().getPositionMatrix();

		RenderSystem.enableCull();
		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();
		RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
		
		this.mtlObjMap
			.entrySet()
			.forEach(entry -> this.renderObj(entry.getValue(), this.mtlMap.get(entry.getKey()), positionMatrix));

		VertexBuffer.unbind();
		RenderSystem.disableBlend();
		RenderSystem.enableCull();
		RenderSystem.depthFunc(GL11.GL_LEQUAL);
	}

	private void renderObj(final Obj obj, final Mtl mtl, final Matrix4f positionMatrix) {
		if (mtl != null && mtl.getMapKd() != null)
			RenderSystem.setShaderTexture(0, this.resolveId(mtl.getMapKd()));

		VertexBuffer vertexBuffer = this.objBufferMap.get(obj);
		vertexBuffer.bind();
		vertexBuffer.draw(positionMatrix, RenderSystem.getProjectionMatrix(), GameRenderer.getPositionTexColorNormalProgram());
	}

	private void bake() {
		if (this.baked) return;

		this.mtlObjMap
			.entrySet()
			.forEach(entry -> this.bakeObj(entry.getValue(), this.mtlMap.get(entry.getKey())));

		this.baked = true;
	}

	private void bakeObj(final Obj obj, final Mtl mtl) {
		final BufferBuilder buffer = Tessellator.getInstance().getBuffer();

		buffer.begin(DrawMode.TRIANGLES, VertexFormats.POSITION_TEXTURE_COLOR_NORMAL);

		this.fillBuffer(obj, mtl, buffer);

		final BufferBuilder.BuiltBuffer builtBuffer = buffer.end();
		final VertexBuffer vertexBuffer = new VertexBuffer(VertexBuffer.Usage.STATIC);
		vertexBuffer.bind();
		vertexBuffer.upload(builtBuffer);
		VertexBuffer.unbind();
				
		objBufferMap.put(obj, vertexBuffer);
	}

	private void fillBuffer(final Obj obj, final Mtl mtl, final BufferBuilder buffer) {
		for (int i = 0; i < obj.getNumFaces(); i++) {
			final ObjFace face = obj.getFace(i);				
			
			for (int j = 0; j < face.getNumVertices(); j++) {
				final FloatTuple vertex = obj.getVertex(face.getVertexIndex(j));
				final FloatTuple texture = obj.getTexCoord(face.getTexCoordIndex(j));
				final FloatTuple normals = obj.getNormal(face.getNormalIndex(j));
				final FloatTuple color = this.nullishCoalescing(mtl.getKd(), FloatTuples.create(1f, 1f, 1f));
				
				buffer
					.vertex(vertex.getX(), vertex.getY(), vertex.getZ())
					.texture(texture.getX(), 1 - texture.getY())
					.color(color.getX(), color.getY(), color.getZ(), 1f)
					.normal(normals.getX(), normals.getY(), normals.getZ())
					.next();
			}
		}
	}

	private void load() throws IOException {
		try (InputStream objStream = this.resolveInputStream(this.modelId)) {
			final Obj obj = ObjUtils.convertToRenderable(ObjReader.read(objStream));
			
			for (String mtlFile : obj.getMtlFileNames()) {
				try (InputStream mtlStream = this.resolveInputStream(mtlFile)) {
					MtlReader
						.read(mtlStream)
						.forEach(mtl -> this.mtlMap.put(mtl.getName(), mtl));
				}
			}

			this.mtlObjMap = ObjSplitting.splitByMaterialGroups(obj);
		}
	}

	private InputStream resolveInputStream(String resolvablePath) throws IOException {
		return this.resolveInputStream(this.resolveId(resolvablePath));
	}

	private InputStream resolveInputStream(Identifier id) throws IOException {
		final ResourceManager manager = MinecraftClient.getInstance().getResourceManager();
		final Optional<Resource> resource = manager.getResource(id);

		if (resource.isEmpty())
			throw new FileNotFoundException(String.format("The provided path cannot be found in %s", id));

		return resource.get().getInputStream();
	}

	private Identifier resolveId(String resolvablePath) {
		if (resolvablePath.split(":").length > 1) {
			return Identifier.tryParse(resolvablePath);
		} else {
			final String newPath = modelId.getPath().substring(0, modelId.getPath().lastIndexOf("/") + 1) + resolvablePath;
			
			return modelId.withPath(newPath);
		}
	}

	@SuppressWarnings("unchecked")
	private <T> T nullishCoalescing(T... values) {
		for (T value : values) if (value != null) return value;
		
		return null;
	}
}
