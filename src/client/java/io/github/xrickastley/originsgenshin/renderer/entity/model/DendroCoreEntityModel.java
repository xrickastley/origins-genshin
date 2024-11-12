package io.github.xrickastley.originsgenshin.renderer.entity.model;

import com.google.common.collect.ImmutableList;

import io.github.xrickastley.originsgenshin.OriginsGenshin;
import io.github.xrickastley.originsgenshin.entity.DendroCoreEntity;

import net.minecraft.client.model.Dilation;
import net.minecraft.client.model.ModelData;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.model.ModelPartBuilder;
import net.minecraft.client.model.ModelPartData;
import net.minecraft.client.model.ModelTransform;
import net.minecraft.client.model.TexturedModelData;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.client.render.entity.model.EntityModelPartNames;
import net.minecraft.client.util.math.MatrixStack;

public class DendroCoreEntityModel extends EntityModel<DendroCoreEntity> {
	public static final EntityModelLayer MODEL_LAYER = new EntityModelLayer(OriginsGenshin.identifier("cube"), "all");

	private final ModelPart base;
	
	public DendroCoreEntityModel(ModelPart root) {
		this.base = root.getChild(EntityModelPartNames.ROOT);
	}

	public static TexturedModelData getTexturedModelData() {
		ModelData modelData = new ModelData();

		ModelPartData modelPartData = modelData.getRoot();
		ModelPartData all = modelPartData.addChild(EntityModelPartNames.ROOT, ModelPartBuilder.create(), ModelTransform.pivot(0.0F, 24.0F, 0.0F));

		all.addChild("layer0", ModelPartBuilder.create().uv(0, 0).cuboid(-1.0F, -2.0F, -1.0F, 2.0F, 1.0F, 2.0F, new Dilation(0.0F)), ModelTransform.pivot(0.0F, 1.0F, 0.0F));
		all.addChild("layer1", ModelPartBuilder.create().uv(0, 0).cuboid(-2.0F, -2.0F, -2.0F, 4.0F, 1.0F, 4.0F, new Dilation(0.0F)), ModelTransform.pivot(0.0F, 0.0F, 0.0F));

		ModelPartData layer2 = all.addChild(
			"layer2", 
			ModelPartBuilder
				.create()
				.uv(0, 0).cuboid(2.0F, -4.0F, -2.0F, 1.0F, 2.0F, 4.0F, new Dilation(0.0F))
				.uv(0, 0).cuboid(-3.0F, -4.0F, -2.0F, 1.0F, 2.0F, 4.0F, new Dilation(0.0F)), 
			ModelTransform.pivot(0.0F, 0.0F, 0.0F)
		);

		layer2.addChild("cube_r1", ModelPartBuilder.create().uv(0, 0).cuboid(0.0F, -2.0F, -2.0F, 1.0F, 2.0F, 4.0F, new Dilation(0.0F)), ModelTransform.of(0.0F, -2.0F, -2.0F, 0.0F, 1.5708F, 0.0F));
		layer2.addChild("cube_r2", ModelPartBuilder.create().uv(0, 0).cuboid(0.0F, -2.0F, -2.0F, 1.0F, 2.0F, 4.0F, new Dilation(0.0F)), ModelTransform.of(0.0F, -2.0F, 3.0F, 0.0F, 1.5708F, 0.0F));

		ModelPartData layer3 = all.addChild(
			"layer3", 
			ModelPartBuilder
				.create()
				.uv(0, 0).cuboid(3.0F, -4.0F, -2.0F, 1.0F, 3.0F, 4.0F, new Dilation(0.0F))
				.uv(0, 0).cuboid(-4.0F, -4.0F, -2.0F, 1.0F, 3.0F, 4.0F, new Dilation(0.0F))
				.uv(3, 3).cuboid(-3.0F, -4.0F, -3.0F, 1.0F, 3.0F, 1.0F, new Dilation(0.0F))
				.uv(3, 3).cuboid(-3.0F, -4.0F, 2.0F, 1.0F, 3.0F, 1.0F, new Dilation(0.0F))
				.uv(3, 3).cuboid(2.0F, -4.0F, 2.0F, 1.0F, 3.0F, 1.0F, new Dilation(0.0F))
				.uv(3, 3).cuboid(2.0F, -4.0F, -3.0F, 1.0F, 3.0F, 1.0F, new Dilation(0.0F)), 
			ModelTransform.pivot(0.0F, -3.0F, 0.0F)
		);

		layer3.addChild("cube_r3", ModelPartBuilder.create().uv(0, 0).cuboid(0.0F, -3.0F, -2.0F, 1.0F, 3.0F, 4.0F, new Dilation(0.0F)), ModelTransform.of(0.0F, -1.0F, -3.0F, 0.0F, 1.5708F, 0.0F));
		layer3.addChild("cube_r4", ModelPartBuilder.create().uv(0, 0).cuboid(0.0F, -3.0F, -2.0F, 1.0F, 3.0F, 4.0F, new Dilation(0.0F)), ModelTransform.of(0.0F, -1.0F, 4.0F, 0.0F, 1.5708F, 0.0F));

		ModelPartData layer4 = all.addChild(
			"layer4", 
			ModelPartBuilder
				.create()
				.uv(0, 0).cuboid(2.0F, -3.0F, -2.0F, 1.0F, 1.0F, 4.0F, new Dilation(0.0F))
				.uv(0, 0).cuboid(-3.0F, -3.0F, -2.0F, 1.0F, 1.0F, 4.0F, new Dilation(0.0F)),
			ModelTransform.pivot(0.0F, -5.0F, 0.0F)
		);

		layer4.addChild("cube_r5", ModelPartBuilder.create().uv(0, 0).cuboid(0.0F, -1.0F, -2.0F, 1.0F, 1.0F, 4.0F, new Dilation(0.0F)), ModelTransform.of(0.0F, -2.0F, -2.0F, 0.0F, 1.5708F, 0.0F));
		layer4.addChild("cube_r6", ModelPartBuilder.create().uv(0, 0).cuboid(0.0F, -1.0F, -2.0F, 1.0F, 1.0F, 4.0F, new Dilation(0.0F)), ModelTransform.of(0.0F, -2.0F, 3.0F, 0.0F, 1.5708F, 0.0F));

		all.addChild("layer5", ModelPartBuilder.create().uv(0, 0).cuboid(-2.0F, -2.0F, -2.0F, 4.0F, 1.0F, 4.0F, new Dilation(0.0F)), ModelTransform.pivot(0.0F, -7.0F, 0.0F));
		all.addChild("layer6", ModelPartBuilder.create().uv(0, 0).cuboid(-1.0F, -3.0F, -1.0F, 2.0F, 2.0F, 2.0F, new Dilation(0.0F)), ModelTransform.pivot(0.0F, -8.0F, 0.0F));
		
		return TexturedModelData.of(modelData, 16, 16);
	}

	@Override
	public void setAngles(DendroCoreEntity entity, float limbAngle, float limbDistance, float animationProgress, float headYaw, float headPitch) {

	}

	@Override
	public void animateModel(DendroCoreEntity entity, float limbAngle, float limbDistance, float tickDelta) {
		float progress = (entity.age + tickDelta) % 60 == 0
			? 1
			: (entity.age + tickDelta) % 60;

		base.setAngles(0, -progress * 0.05f, 0);
	}

	@Override
	public void render(MatrixStack matrices, VertexConsumer vertexConsumer, int light, int overlay, float red, float green, float blue, float alpha) {
		ImmutableList
			.of(base)
			.forEach(modelRenderer -> modelRenderer.render(matrices, vertexConsumer, light, overlay, red, green, blue, alpha));
	}
}
