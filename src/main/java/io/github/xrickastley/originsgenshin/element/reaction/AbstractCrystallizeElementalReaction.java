package io.github.xrickastley.originsgenshin.element.reaction;

import io.github.xrickastley.originsgenshin.element.ElementalApplication;

import net.minecraft.entity.LivingEntity;

import javax.annotation.Nullable;

public abstract sealed class AbstractCrystallizeElementalReaction
	extends ElementalReaction
	permits PyroCrystallizeElementalReaction, HydroCrystallizeElementalReaction, ElectroCrystallizeElementalReaction, CryoCrystallizeElementalReaction, FrozenCrystallizeElementalReaction
{
	AbstractCrystallizeElementalReaction(ElementalReactionSettings settings) {
		super(settings);
	}

	@Override
	protected void onReaction(LivingEntity entity, ElementalApplication auraElement, ElementalApplication triggeringElement, double reducedGauge, @Nullable LivingEntity origin) {}
}
