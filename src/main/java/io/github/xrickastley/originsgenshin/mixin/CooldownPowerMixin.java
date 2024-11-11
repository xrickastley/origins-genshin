package io.github.xrickastley.originsgenshin.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;

import io.github.apace100.apoli.power.CooldownPower;
import io.github.apace100.apoli.power.HudRendered;
import io.github.apace100.apoli.power.Power;
import io.github.apace100.apoli.power.PowerType;
import io.github.apace100.apoli.util.HudRender;
import io.github.xrickastley.originsgenshin.factory.OriginsGenshinStatusEffects;
import net.minecraft.entity.LivingEntity;

/**
 * Prioritized since Frozen **MUST** disable using powers.
 */
@Pseudo
@Mixin(value = CooldownPower.class, priority = Integer.MIN_VALUE)
public abstract class CooldownPowerMixin 
	extends Power 
	implements HudRendered
{
	public CooldownPowerMixin(PowerType<?> type, LivingEntity entity, int cooldownDuration, HudRender hudRender) {
		super(type, entity);

		throw new AssertionError();
	}

	@ModifyReturnValue(
		method = "canUse",
		at = @At("RETURN"),
		remap = false
	)
	private boolean frozen_CantUsePowers(boolean original) {
		return original && !this.entity.hasStatusEffect(OriginsGenshinStatusEffects.FROZEN);
	}
}
