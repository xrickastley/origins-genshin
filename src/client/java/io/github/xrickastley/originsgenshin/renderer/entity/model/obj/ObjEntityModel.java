package io.github.xrickastley.originsgenshin.renderer.entity.model.obj;

import java.io.FileNotFoundException;
import java.io.IOException;

import io.github.xrickastley.originsgenshin.util.ObjRenderer;

import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.util.Identifier;

public final class ObjEntityModel<T extends Entity> extends EntityModel<T> {
	private final Identifier objId;
	private final ObjRenderer renderer;

	public ObjEntityModel(Identifier objId) throws IOException, FileNotFoundException {
		this.objId = objId;
		this.renderer = new ObjRenderer(this.objId.withSuffixedPath(".obj"));
	}

	@Override
	public void setAngles(T entity, float limbAngle, float limbDistance, float animationProgress, float headYaw, float headPitch) {}

	@Override
	public void render(MatrixStack matrices, VertexConsumer vertices, int light, int overlay, float red, float green, float blue, float alpha) {
		this.renderer.render(matrices);
	}
}
