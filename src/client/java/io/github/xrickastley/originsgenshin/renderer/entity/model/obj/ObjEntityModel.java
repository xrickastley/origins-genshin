package io.github.xrickastley.originsgenshin.renderer.entity.model.obj;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.function.Function;

import io.github.xrickastley.originsgenshin.util.ObjRenderer;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.resource.Resource;
import net.minecraft.util.Identifier;

public final class ObjEntityModel<T extends Entity> extends EntityModel<T> {
	private final Identifier objId;
	private final ObjRenderer renderer;

	public ObjEntityModel(Identifier objId) {
		this.objId = objId;
		this.renderer = new ObjRenderer();
	}

	public ObjEntityModel<T> loadModel() throws IOException, FileNotFoundException {
		final Identifier objPath = this.objId.withSuffixedPath(".obj");
		final Identifier mtlPath = this.objId.withSuffixedPath(".mtl");

		final Resource objResource = this.getResource(objPath, id -> "Could not find OBJ file: " + id);
		final InputStream objStream = objResource.getInputStream();

		final Resource mtlResource = this.getResource(mtlPath, id -> "Could not find MTL file: " + id);
		final InputStream mtlStream = mtlResource.getInputStream();
		
		this.renderer.loadObj(objStream, mtlStream);

		return this;
	}

	@Override
	public void setAngles(T entity, float limbAngle, float limbDistance, float animationProgress, float headYaw, float headPitch) {}

	@Override
	public void render(MatrixStack matrices, VertexConsumer vertices, int light, int overlay, float red, float green, float blue, float alpha) {
		this.renderer.render(matrices, vertices, light, overlay, red, green, blue, alpha);
	}

	private Resource getResource(Identifier id, Function<Identifier, String> msgSupplier) throws FileNotFoundException {
		return MinecraftClient
			.getInstance()
			.getResourceManager()
			.getResource(id)
			.orElseThrow(() -> new FileNotFoundException(msgSupplier.apply(id)));
	}
}
