package io.github.xrickastley.originsgenshin.mixins;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MovementType;
import net.minecraft.util.math.Vec3d;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import io.github.xrickastley.originsgenshin.factories.OriginsGenshinStatusEffects;

/**
 * Prioritized since Frozen **MUST** disable movement.
 */
@Mixin(value = Entity.class, priority = Integer.MIN_VALUE)
public abstract class EntityMixin {
	@Inject(
		method = "move",
		at = @At("HEAD"),
		cancellable = true
	)
	private void frozen_CantMove(MovementType movementType, Vec3d movement, CallbackInfo info) {
		if (!((Entity)(Object) this instanceof final LivingEntity entity)) return;

		if (entity.hasStatusEffect(OriginsGenshinStatusEffects.FROZEN)) {
			entity.setVelocity(0, 0, 0);

			info.cancel();
		}
	}
}