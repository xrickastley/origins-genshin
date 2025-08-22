package io.github.xrickastley.originsgenshin.mixin;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;

import java.util.ArrayList;
import java.util.List;

import org.spongepowered.asm.mixin.Debug;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import io.github.xrickastley.originsgenshin.component.ElementComponent;
import io.github.xrickastley.originsgenshin.interfaces.IPlayerEntity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;

@Mixin(PlayerEntity.class)
@Debug(export = true)
public abstract class PlayerEntityMixin implements IPlayerEntity {
	@Unique
	private List<DamageSource> originsgenshin$critDamageSources = new ArrayList<>();

	@Unique
	@Override
	public boolean originsgenshin$isCrit(DamageSource source) {
		return this.originsgenshin$critDamageSources != null && this.originsgenshin$critDamageSources.contains(source);
	}

	// why are there two seperate knockbacks :sob:
	@Definition(id = "i", local = @Local(type = int.class, ordinal = 0))
	@Expression("i > 0")
	@ModifyExpressionValue(
		method = "attack",
		at = @At("MIXINEXTRAS:EXPRESSION")
	)
	private boolean preventKnockbackIfCrystallize(boolean original, @Local(argsOnly = true) Entity entity) {
		if (!(entity instanceof final LivingEntity livingEntity)) return original;

		final ElementComponent component = ElementComponent.KEY.get(livingEntity);

		return original && !component.reducedCrystallizeShield();
	}

	@ModifyArg(
		method = "attack",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/entity/Entity;damage(Lnet/minecraft/entity/damage/DamageSource;F)Z"
		),
		index = 0
	)
	private DamageSource checkForCritMain(DamageSource source, @Local(ordinal = 2) boolean crit) {
		if (originsgenshin$critDamageSources == null) originsgenshin$critDamageSources = new ArrayList<>();

		if (crit) originsgenshin$critDamageSources.add(source);

		return source;
	}

	@ModifyArg(
		method = "attack",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/entity/LivingEntity;damage(Lnet/minecraft/entity/damage/DamageSource;F)Z"
		),
		index = 0
	)
	private DamageSource checkForCritSweep(DamageSource source, @Local(ordinal = 2) boolean crit) {
		if (originsgenshin$critDamageSources == null) originsgenshin$critDamageSources = new ArrayList<>();

		if (crit) originsgenshin$critDamageSources.add(source);

		return source;
	}

	@Inject(
		method = "tick",
		at = @At("HEAD")
	)
	private void removeCritDS(CallbackInfo ci) {
		if (originsgenshin$critDamageSources != null)
			originsgenshin$critDamageSources.clear();
		else
			originsgenshin$critDamageSources = new ArrayList<>();
	}
}
