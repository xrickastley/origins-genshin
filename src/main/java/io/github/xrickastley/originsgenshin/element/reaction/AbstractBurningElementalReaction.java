package io.github.xrickastley.originsgenshin.element.reaction;

import javax.annotation.Nullable;

import io.github.xrickastley.originsgenshin.OriginsGenshin;
import io.github.xrickastley.originsgenshin.component.ElementComponent;
import io.github.xrickastley.originsgenshin.element.Element;
import io.github.xrickastley.originsgenshin.element.ElementHolder;
import io.github.xrickastley.originsgenshin.element.ElementalApplication;
import io.github.xrickastley.originsgenshin.element.InternalCooldownContext;
import io.github.xrickastley.originsgenshin.element.InternalCooldownType;
import io.github.xrickastley.originsgenshin.events.ElementRemoved;
import net.minecraft.entity.LivingEntity;

public abstract class AbstractBurningElementalReaction extends ElementalReaction {
	private static final InternalCooldownType BURNING_PYRO_ICD = InternalCooldownType.registered(OriginsGenshin.identifier("reactions/burning/pyro_icd"), 40, 3);

	protected AbstractBurningElementalReaction(ElementalReactionSettings settings) {
		super(settings);

		// Reactions against the Burning aura consume both Burning and Pyro *equally*
	}

	@Override
	public boolean isTriggerable(LivingEntity entity) {
		return super.isTriggerable(entity) && ElementComponent.KEY.get(entity).hasElementalApplication(Element.BURNING);
	}

	public static void reduceBurningGauge(ElementalApplication auraElement, ElementalApplication triggeringElement, LivingEntity entity, double reducedGauge) {
		if (auraElement.getElement() != Element.PYRO || triggeringElement.getElement() != Element.PYRO) return;

		final ElementComponent component = ElementComponent.KEY.get(entity);

		if (!component.hasElementalApplication(Element.BURNING)) return;

		component
			.getElementHolder(Element.BURNING)
			.getElementalApplication()
			.reduceGauge(reducedGauge);
	}

	@Override
	protected void onReaction(LivingEntity entity, ElementalApplication auraElement, ElementalApplication triggeringElement, double reducedGauge, @Nullable LivingEntity origin) {
		ElementComponent.KEY
			.get(entity)
			.getElementHolder(Element.BURNING)
			.setElementalApplication(
				ElementalApplication.gaugeUnits(entity, Element.BURNING, 2.0f, false)
			);

		// TODO: VALIDATE - add custom ICDs, change this to custom ICD w/ 2s ICD.
		// TODO: move to damage like Electro-Charged
		ElementComponent.KEY
			.get(entity)
			.addElementalApplication(Element.PYRO, InternalCooldownContext.ofType(origin, "origins-genshin:reactions/burning", BURNING_PYRO_ICD), 1.0f);

		// TODO: Burning refresh: applying Dendro while the Burning aura is active will **always** overwrite the existing aura.
		// 	probably hard code that, too niche? 
	}

	static {
		ElementRemoved.EVENT.register(application -> {
			if (application == null || application.getElement() != Element.DENDRO) return;

			final ElementComponent component = ElementComponent.KEY.get(application.getEntity());

			component
				.getElementHolder(Element.BURNING)
				.setElementalApplication(null);
		});
	}
}
