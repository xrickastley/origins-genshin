package io.github.xrickastley.originsgenshin.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import io.github.xrickastley.originsgenshin.element.Element;
import io.github.xrickastley.originsgenshin.element.ElementalApplications;
import io.github.xrickastley.originsgenshin.element.ElementalDamageSource;
import io.github.xrickastley.originsgenshin.element.InternalCooldownContext;
import io.github.xrickastley.originsgenshin.element.InternalCooldownType;
import io.github.xrickastley.originsgenshin.factory.OriginsGenshinGameRules;
import io.github.xrickastley.originsgenshin.factory.OriginsGenshinStatusEffects;

// Prioritized since Frozen **MUST** disable movement.
@Mixin(value = Entity.class, priority = Integer.MIN_VALUE)
public abstract class EntityMixin {
	@Shadow
	public abstract World getWorld();

	@Final
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

	@Final
	@ModifyArg(
		method = "onStruckByLightning",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/entity/Entity;damage(Lnet/minecraft/entity/damage/DamageSource;F)Z",
			ordinal = 0
		)
	)
	private DamageSource applyElectroOnLightning(DamageSource source) {
		return (Entity)(Object) this instanceof final LivingEntity entity && this.getWorld().getGameRules().getBoolean(OriginsGenshinGameRules.ELECTRO_FROM_LIGHTNING)
			? new ElementalDamageSource(source, ElementalApplications.gaugeUnits(entity, Element.ELECTRO, 2.0), InternalCooldownContext.ofType(entity, "origins-genshin:natural_environment", InternalCooldownType.INTERVAL_ONLY))
			: source;
	}
}