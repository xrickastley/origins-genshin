package io.github.xrickastley.originsgenshin.renderer.entity;

import io.github.xrickastley.originsgenshin.OriginsGenshin;
import io.github.xrickastley.originsgenshin.entity.DendroCoreEntity;
import io.github.xrickastley.originsgenshin.renderer.entity.model.DendroCoreEntityModel;
import io.github.xrickastley.originsgenshin.renderer.entity.model.obj.ObjEntityModel;
import io.github.xrickastley.originsgenshin.util.Ease;
import io.github.xrickastley.originsgenshin.util.MathHelper2;

import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.RotationAxis;

public class DendroCoreEntityRenderer extends LivingEntityRenderer<DendroCoreEntity, EntityModel<DendroCoreEntity>> {
	public DendroCoreEntityRenderer(EntityRendererFactory.Context context) {
		super(
			context, 
			DendroCoreEntityRenderer.createModel(context),
			0.5f
		);
	}

	private static EntityModel<DendroCoreEntity> createModel(EntityRendererFactory.Context context) {
		try {
			return new ObjEntityModel<DendroCoreEntity>(OriginsGenshin.identifier("models/entity/dendro_core"));
		} catch (Exception e) {
			OriginsGenshin
				.sublogger(DendroCoreEntityRenderer.class)
				.info("An exception occured while trying to load ObjEntityModel, resorting to fallback...", e);
			
			return new DendroCoreEntityModel(context.getPart(DendroCoreEntityModel.MODEL_LAYER));
		}
	}

	public Identifier getTexture(DendroCoreEntity entity) {
		return OriginsGenshin.identifier("textures/entity/dendro_core/dendro_core.png");
	}

	@Override
	public void render(DendroCoreEntity dendroCore, float f, float g, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i) {
		this.shadowOpacity = 0f;
		this.shadowRadius = 0f;

		super.render(dendroCore, f, g, matrixStack, vertexConsumerProvider, i);
	}

	@Override
	protected void scale(DendroCoreEntity dendroCore, MatrixStack matrixStack, float delta) {
		final double explodeProgress = Ease.IN_QUAD.applyLerp(MathHelper2.endOffset(dendroCore.age + delta, 2, 0, 120), 0, 1.5);
		final float scale = !dendroCore.isHyperbloom()
			? 3f + (float) (explodeProgress * 7.5)
			: 2f;

		matrixStack.scale(scale, scale, scale);
		matrixStack.translate(0, -1.480, 0);
		matrixStack.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(180.0f));
	}

	@Override
	protected void renderLabelIfPresent(DendroCoreEntity entity, Text text, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light) {
		return;
	}
}
