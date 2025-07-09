package io.github.xrickastley.originsgenshin.element.reaction;

import javax.annotation.Nullable;

import io.github.xrickastley.originsgenshin.OriginsGenshin;
import io.github.xrickastley.originsgenshin.component.ElementComponent;
import io.github.xrickastley.originsgenshin.element.Element;
import io.github.xrickastley.originsgenshin.element.ElementalApplication;
import io.github.xrickastley.originsgenshin.element.InternalCooldownContext;
import io.github.xrickastley.originsgenshin.element.InternalCooldownType;
import net.minecraft.entity.LivingEntity;

public abstract class AbstractBurningElementalReaction extends ElementalReaction {
	private static final InternalCooldownType BURNING_PYRO_ICD = InternalCooldownType.registered(OriginsGenshin.identifier("reactions/burning/pyro_icd"), 40, 3);

	protected AbstractBurningElementalReaction(ElementalReactionSettings settings) {
		super(settings);

		// Reactions against the Burning aura consume both Burning and Pyro *equally*
	}

	@Override
	protected void onReaction(LivingEntity entity, ElementalApplication auraElement, ElementalApplication triggeringElement, double reducedGauge, @Nullable LivingEntity origin) {
		ElementComponent.KEY
			.get(entity)
			.addElementalApplication(Element.BURNING, InternalCooldownContext.ofNone(origin), 2.0f);
			
		// TODO: VALIDATE - add custom ICDs, change this to custom ICD w/ 2s ICD.
		ElementComponent.KEY
			.get(entity)
			.addElementalApplication(Element.PYRO, InternalCooldownContext.ofType(origin, "origins-genshin:reactions/burning", BURNING_PYRO_ICD), 1.0f);

		// TODO: Burning refresh: applying Dendro while the Burning aura is active will **always** overwrite the existing aura.
		// 	probably hard code that, too niche? 
	}
}
