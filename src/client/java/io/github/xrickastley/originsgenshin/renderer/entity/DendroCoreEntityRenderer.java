package io.github.xrickastley.originsgenshin.renderer.entity;

import io.github.xrickastley.originsgenshin.OriginsGenshin;
import io.github.xrickastley.originsgenshin.entity.DendroCoreEntity;
import io.github.xrickastley.originsgenshin.renderer.entity.model.DendroCoreEntityModel;
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
	public void render(DendroCoreEntity livingEntity, float f, float g, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i) {
		matrixStack.push();
		matrixStack.scale(0.625f, 0.625f, 0.625f);

		super.render(livingEntity, f, g, matrixStack, vertexConsumerProvider, i);

		matrixStack.pop();
	}

	@Override
	protected void renderLabelIfPresent(DendroCoreEntity entity, Text text, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light) {
		return;
	}
}
