package io.github.xrickastley.originsgenshin.mixin;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import io.github.xrickastley.originsgenshin.component.ElementComponent;

import org.spongepowered.asm.mixin.Debug;
import org.spongepowered.asm.mixin.Mixin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(PlayerEntity.class)
@Debug(export = true)
public class PlayerEntityMixin {
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
}
