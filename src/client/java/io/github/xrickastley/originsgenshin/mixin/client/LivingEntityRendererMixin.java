package io.github.xrickastley.originsgenshin.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

import io.github.xrickastley.originsgenshin.factory.OriginsGenshinStatusEffects;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;

@Environment(EnvType.CLIENT)
@Mixin(value = LivingEntityRenderer.class, priority = Integer.MAX_VALUE)
public abstract class LivingEntityRendererMixin<T extends LivingEntity, M extends EntityModel<T>> extends EntityRenderer<T>  {
	protected LivingEntityRendererMixin(EntityRendererFactory.Context context) {
		super(context);
		
		throw new AssertionError();
	}

    @ModifyArgs(
		method = "render", 
		at = @At(
			value = "INVOKE", 
			target = "Lnet/minecraft/client/render/entity/model/EntityModel;render(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumer;IIFFFF)V"
		)
	)
	private void renderFrostedModel(Args args, LivingEntity livingEntity, float f, float tickDelta, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i) {
		if (!livingEntity.hasStatusEffect(OriginsGenshinStatusEffects.FROZEN)) return;

		// args.set(4, 0.815f);
		// args.set(5, 1f);
		args.set(4, 0.25f);
		args.set(5, 0.5f);
		args.set(6, 1f);
		args.set(7, 1f);
	}
}
	