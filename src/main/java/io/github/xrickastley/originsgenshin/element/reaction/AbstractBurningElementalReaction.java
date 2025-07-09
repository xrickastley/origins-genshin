package io.github.xrickastley.originsgenshin.element.reaction;

import javax.annotation.Nullable;

import io.github.xrickastley.originsgenshin.component.ElementComponent;
import io.github.xrickastley.originsgenshin.element.Element;
import io.github.xrickastley.originsgenshin.element.ElementalApplication;
import io.github.xrickastley.originsgenshin.element.InternalCooldownContext;
import net.minecraft.entity.LivingEntity;

public abstract class AbstractBurningElementalReaction extends ElementalReaction {
	protected AbstractBurningElementalReaction(ElementalReactionSettings settings) {
		super(settings);

		// Reactions against the Burning aura consume both Burning and Pyro *equally*
	}

	@Override
	protected void onReaction(LivingEntity entity, ElementalApplication auraElement, ElementalApplication triggeringElement, double reducedGauge, @Nullable LivingEntity origin) {
		ElementComponent.KEY
			.get(entity)
			.addElementalApplication(Element.BURNING, InternalCooldownContext.ofNone(origin), 2.0f);
			
		// TODO: add custom ICDs, change this to custom ICD w/ 2s ICD.
		ElementComponent.KEY
			.get(entity)
			.addElementalApplication(Element.PYRO, InternalCooldownContext.ofNone(origin), 1.0f);

		// TODO: Burning refresh: applying Dendro while the Burning aura is active will **always** overwrite the existing aura.
		// 	probably hard code that, too niche? 
	}
}
