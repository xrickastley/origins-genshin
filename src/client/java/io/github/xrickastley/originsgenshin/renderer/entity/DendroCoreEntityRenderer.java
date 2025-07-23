package io.github.xrickastley.originsgenshin.renderer.entity;

import io.github.xrickastley.originsgenshin.OriginsGenshin;
import io.github.xrickastley.originsgenshin.entity.DendroCoreEntity;
import io.github.xrickastley.originsgenshin.renderer.entity.model.DendroCoreEntityModel;
import io.github.xrickastley.originsgenshin.util.Ease;
import io.github.xrickastley.originsgenshin.util.MathHelper2;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class DendroCoreEntityRenderer extends LivingEntityRenderer<DendroCoreEntity, DendroCoreEntityModel> {
	public DendroCoreEntityRenderer(EntityRendererFactory.Context context) {
		super(
			context, 
			new DendroCoreEntityModel(
				context.getPart(DendroCoreEntityModel.MODEL_LAYER)
			), 
			0.5f
		);
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
		if (dendroCore.isHyperbloom()) {
			matrixStack.scale(0.5f, 0.5f, 0.5f);

			return;
		}

		final double explodeProgress = Ease.IN_QUAD.applyLerp(MathHelper2.endOffset(dendroCore.age + delta, 2, 0, 120), 0, 1.5);
		final float scale = 0.625f + (float) (explodeProgress * 1.25);

		matrixStack.scale(scale, scale, scale);
	}

	@Override
	protected void renderLabelIfPresent(DendroCoreEntity entity, Text text, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light) {
		return;
	}
}
