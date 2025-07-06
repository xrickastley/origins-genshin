package io.github.xrickastley.originsgenshin.mixin.client;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import io.github.xrickastley.originsgenshin.component.FrozenEffectComponent;

import java.util.function.Function;

import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

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

	/*
    @Inject(method = { "render(Lnet/minecraft/entity/LivingEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V" }, at = { @At("HEAD") }, cancellable = true)
    private void enchancement$frostbite(final T livingEntity, final float yaw, final float tickDelta, final MatrixStack matrices, final VertexConsumerProvider vertexConsumers, final int light, final CallbackInfo ci) {
        if (this instanceof final FrozenPlayerEntityRenderer frozenPlayerEntityRenderer) {
            if (livingEntity instanceof final FrozenPlayerEntity frozenPlayer) {
                if (frozenPlayer.getDataTracker().get(FrozenPlayerEntity.SLIM)) {
                    this.field_4737 = (M)frozenPlayerEntityRenderer.slimModel;
                }
                else {
                    this.field_4737 = (M)frozenPlayerEntityRenderer.defaultModel;
                }
            }
        }
        final FrozenComponent frozenComponent = (FrozenComponent)ModEntityComponents.FROZEN.get((Object)livingEntity);
        if (frozenComponent.isFrozen()) {
            final MinecraftClient client = MinecraftClient.getInstance();
            matrices.push();
            livingEntity.setPose(frozenComponent.getForcedPose());
            this.field_4737.handSwingProgress = this.method_4044(livingEntity, tickDelta);
            this.field_4737.riding = livingEntity.hasVehicle();
            this.field_4737.child = livingEntity.isBaby();
            final float bodyYaw = frozenComponent.getForcedBodyYaw();
            float pitch = frozenComponent.getForcedPitch();
            float headYawMinusBodyYaw = frozenComponent.getForcedHeadYaw() - bodyYaw;
            float limbAngle = frozenComponent.getForcedLimbAngle();
            final float limbDistance = frozenComponent.getForcedLimbDistance();
            final float animationProgress = this.method_4045(livingEntity, tickDelta);
            if (LivingEntityRenderer.shouldFlipUpsideDown((LivingEntity)livingEntity)) {
                pitch *= -1.0f;
                headYawMinusBodyYaw *= -1.0f;
            }
            if (livingEntity.isBaby()) {
                limbAngle *= 3.0f;
            }
            this.method_4058(livingEntity, matrices, animationProgress, bodyYaw, tickDelta);
            matrices.scale(-1.0f, -1.0f, 1.0f);
            this.method_4042(livingEntity, matrices, tickDelta);
            matrices.translate(0.0f, -1.501f, 0.0f);
            this.field_4737.animateModel((Entity)livingEntity, limbAngle, limbDistance, tickDelta);
            this.field_4737.setAngles((Entity)livingEntity, limbAngle, limbDistance, 0.0f, headYawMinusBodyYaw, pitch);
            final boolean visible = this.method_4056(livingEntity);
            final boolean translucent = !visible && !livingEntity.isInvisibleTo((PlayerEntity)client.player);
            final RenderLayer renderLayer = this.method_24302(livingEntity, visible, translucent, client.hasOutline((Entity)livingEntity));
            if (renderLayer != null) {
                this.field_4737.render(matrices, vertexConsumers.getBuffer(renderLayer), light, LivingEntityRenderer.getOverlay((LivingEntity)livingEntity, this.method_23185(livingEntity, tickDelta)), 1.0f, 1.0f, 1.0f, translucent ? 0.15f : 1.0f);
            }
            if (!livingEntity.isSpectator()) {
                for (final FeatureRenderer<T, M> featureRenderer : this.field_4738) {
                    featureRenderer.render(matrices, vertexConsumers, light, (Entity)livingEntity, limbAngle, limbDistance, tickDelta, animationProgress, headYawMinusBodyYaw, pitch);
                }
            }
            matrices.pop();
            super.render((Entity)livingEntity, yaw, tickDelta, matrices, vertexConsumers, light);
            ci.cancel();
        }
    }
    */

	/*
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
		*/
}
	