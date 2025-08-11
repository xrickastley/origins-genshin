package io.github.xrickastley.originsgenshin.mixin.client;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;

import java.util.function.Function;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import io.github.xrickastley.originsgenshin.component.FrozenEffectComponent;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Identifier;

@Environment(EnvType.CLIENT)
@Mixin(value = LivingEntityRenderer.class, priority = Integer.MAX_VALUE)
public abstract class LivingEntityRendererMixin<T extends LivingEntity, M extends EntityModel<T>> extends EntityRenderer<T>  {
	protected LivingEntityRendererMixin(EntityRendererFactory.Context context) {
		super(context);

		throw new AssertionError();
	}

	@Unique
	private FrozenEffectComponent getComponent(LivingEntity entity) {
		return FrozenEffectComponent.KEY.get(entity);
	}

	@Unique
	@SuppressWarnings("hiding")
	private <T> T ifFrozen(LivingEntity entity, Function<FrozenEffectComponent, T> ifFrozen, T ifNotFrozen) {
		final FrozenEffectComponent component = FrozenEffectComponent.KEY.get(entity);

		return component.isFrozen()
			? ifFrozen.apply(component)
			: ifNotFrozen;
	}

	@ModifyExpressionValue(
		method = "getRenderLayer",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/client/render/entity/LivingEntityRenderer;getTexture(Lnet/minecraft/entity/Entity;)Lnet/minecraft/util/Identifier;"
		)
	)
	private Identifier renderFrostedModel(Identifier original, @Local(argsOnly = true) LivingEntity entity) {
		return this.ifFrozen(entity, c -> Identifier.of("minecraft", "textures/block/ice.png"), original);
	}

	@ModifyReturnValue(
		method = "isShaking",
		at = @At("RETURN")
	)
	private boolean isShakingWhenFrozen(boolean original, @Local(argsOnly = true) LivingEntity entity) {
		return original || this.getComponent(entity).isFrozen();
	}

	@Inject(
		method = "render(Lnet/minecraft/entity/LivingEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V",
		at = @At("HEAD")
	)
	private void forceFrozenPose(T livingEntity, float f, float g, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i, CallbackInfo ci, @Local(argsOnly = true) LivingEntity entity) {
		final FrozenEffectComponent component = FrozenEffectComponent.KEY.get(entity);

		if (component.isFrozen()) livingEntity.setPose(component.getForcePose());
	}

	@ModifyExpressionValue(
		method = "render(Lnet/minecraft/entity/LivingEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/util/math/MathHelper;lerpAngleDegrees(FFF)F",
			ordinal = 0
		)
	)
	private float forceFrozenBodyYaw(float original, @Local(argsOnly = true) LivingEntity entity) {
		return this.ifFrozen(entity, FrozenEffectComponent::getForceBodyYaw, original);
	}

	@ModifyExpressionValue(
		method = "render(Lnet/minecraft/entity/LivingEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/util/math/MathHelper;lerpAngleDegrees(FFF)F",
			ordinal = 1
		)
	)
	private float forceFrozenHeadYaw(float original, @Local(argsOnly = true) LivingEntity entity) {
		return this.ifFrozen(entity, FrozenEffectComponent::getForceHeadYaw, original);
	}

	@ModifyExpressionValue(
		method = "render(Lnet/minecraft/entity/LivingEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/util/math/MathHelper;lerp(FFF)F",
			ordinal = 0
		)
	)
	private float forceFrozenPitch(float original, @Local(argsOnly = true) LivingEntity entity) {
		return this.ifFrozen(entity, FrozenEffectComponent::getForcePitch, original);
	}

	@ModifyExpressionValue(
		method = "render(Lnet/minecraft/entity/LivingEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/entity/LimbAnimator;getSpeed(F)F"
		)
	)
	private float forceFrozenLimbDistance(float original, @Local(argsOnly = true) LivingEntity entity) {
		return this.ifFrozen(entity, FrozenEffectComponent::getForceLimbDistance, original);
	}

	@ModifyExpressionValue(
		method = "render(Lnet/minecraft/entity/LivingEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/entity/LimbAnimator;getPos(F)F"
		)
	)
	private float forceFrozenLimbAngle(float original, @Local(argsOnly = true) LivingEntity entity) {
		return this.ifFrozen(entity, FrozenEffectComponent::getForceLimbAngle, original);
	}
}
