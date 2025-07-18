package io.github.xrickastley.originsgenshin.element.reaction;

import javax.annotation.Nullable;

import io.github.xrickastley.originsgenshin.element.ElementalApplication;
import net.minecraft.entity.LivingEntity;

public abstract sealed class AbstractCrystallizeElementalReaction 
	extends ElementalReaction 
	permits PyroCrystallizeElementalReaction, HydroCrystallizeElementalReaction, ElectroCrystallizeElementalReaction, CryoCrystallizeElementalReaction, FrozenCrystallizeElementalReaction
{
	protected AbstractCrystallizeElementalReaction(ElementalReactionSettings settings) {
		super(settings);
	}

	@Override
	protected void onReaction(LivingEntity entity, ElementalApplication auraElement, ElementalApplication triggeringElement, double reducedGauge, @Nullable LivingEntity origin) {}
}
