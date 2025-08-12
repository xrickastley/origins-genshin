package io.github.xrickastley.originsgenshin.element.reaction;

import io.github.xrickastley.originsgenshin.OriginsGenshin;
import io.github.xrickastley.originsgenshin.component.ElementComponent;
import io.github.xrickastley.originsgenshin.element.Element;
import io.github.xrickastley.originsgenshin.element.ElementalApplication;
import io.github.xrickastley.originsgenshin.element.InternalCooldownContext;
import io.github.xrickastley.originsgenshin.factory.OriginsGenshinParticleFactory;
import io.github.xrickastley.originsgenshin.factory.OriginsGenshinStatusEffects;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;

import javax.annotation.Nullable;

public final class FrozenElementalReaction extends ElementalReaction {
	FrozenElementalReaction() {
		super(
			new ElementalReactionSettings("Frozen", OriginsGenshin.identifier("frozen"), OriginsGenshinParticleFactory.FROZEN)
				.setReactionCoefficient(0)
				.setAuraElement(Element.CRYO, 4)
				.setTriggeringElement(Element.HYDRO, 3)
				.reversable(true)
		);
	}

	@Override
	protected void onReaction(LivingEntity entity, ElementalApplication auraElement, ElementalApplication triggeringElement, double _reducedGauge, @Nullable LivingEntity origin) {
		double reducedGauge;

		if (auraElement.getElement() == Element.HYDRO) {
			reducedGauge = auraElement.reduceGauge(reactionCoefficient * triggeringElement.getCurrentGauge());
		} else {
			reducedGauge = auraElement.reduceGauge(Double.MAX_VALUE * triggeringElement.getCurrentGauge());
		}

		// Gauge_FreezeAura = 2 * min(Gauge_OriginAura, Gauge_TriggerElement)
		// Always the min of both.
		final double freezeAuraGauge = 2 * triggeringElement.reduceGauge(reducedGauge);
		// Freeze Duration (Seconds) = 2√(5 * freezeAuraGauge) + 4) - 4
		final double freezeTickDuration = (2.0 * Math.sqrt((5 * freezeAuraGauge) + 4) - 4) * 20;

		ElementComponent.KEY
			.get(entity)
			.addElementalApplication(Element.FROZEN, InternalCooldownContext.ofNone(origin), freezeAuraGauge, freezeTickDuration);

		entity.addStatusEffect(
			new StatusEffectInstance(OriginsGenshinStatusEffects.FROZEN, (int) Math.floor(freezeTickDuration * 1.025))
		);

		OriginsGenshin
			.sublogger(this)
			.debug("Frozen (Elemental Application): {} | Frozen (Status Effect): {}", ElementComponent.KEY.get(entity).getElementalApplication(Element.FROZEN), entity.getStatusEffect(OriginsGenshinStatusEffects.FROZEN));
	}
}
