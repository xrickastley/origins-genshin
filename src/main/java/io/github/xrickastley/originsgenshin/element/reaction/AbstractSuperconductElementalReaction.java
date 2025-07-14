package io.github.xrickastley.originsgenshin.element.reaction;

import javax.annotation.Nullable;

import io.github.xrickastley.originsgenshin.element.ElementalApplication;
import io.github.xrickastley.originsgenshin.factory.OriginsGenshinStatusEffects;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;

public abstract class AbstractSuperconductElementalReaction extends ElementalReaction {
	protected AbstractSuperconductElementalReaction(ElementalReactionSettings settings) {
		super(settings);
	}

	@Override
	protected void onReaction(LivingEntity entity, ElementalApplication auraElement, ElementalApplication triggeringElement, double reducedGauge, @Nullable LivingEntity origin) {
		

		entity.addStatusEffect(new StatusEffectInstance(OriginsGenshinStatusEffects.SUPERCONDUCT, 240, 1), origin);
	}
}