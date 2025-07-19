package io.github.xrickastley.originsgenshin.element.reaction;

import javax.annotation.Nullable;

import io.github.xrickastley.originsgenshin.OriginsGenshin;
import io.github.xrickastley.originsgenshin.component.ElementComponent;
import io.github.xrickastley.originsgenshin.element.Element;
import io.github.xrickastley.originsgenshin.element.ElementalApplication;
import io.github.xrickastley.originsgenshin.element.InternalCooldownContext;
import io.github.xrickastley.originsgenshin.factory.OriginsGenshinParticleFactory;
import io.github.xrickastley.originsgenshin.factory.OriginsGenshinStatusEffects;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;

public final class FrozenElementalReaction extends ElementalReaction {
	FrozenElementalReaction() {
		super(
			new ElementalReactionSettings("Frozen", OriginsGenshin.identifier("frozen"), OriginsGenshinParticleFactory.FROZEN)
				// Triggering Frozen should consume the entirety of both Cryo and Hydro aura.
				.setReactionCoefficient(Double.MAX_VALUE)
				.setAuraElement(Element.CRYO, 4)
				.setTriggeringElement(Element.HYDRO, 3)
				.reversable(true)
		);
	}

	@Override
	protected void onReaction(LivingEntity entity, ElementalApplication auraElement, ElementalApplication triggeringElement, double reducedGauge, @Nullable LivingEntity origin) {
		// Gauge-FreezeAura = 2 * min(Gauge-OriginAura, Gauge-TriggerElement)
		final double freezeAuraGauge = 2 * Math.min(auraElement.getCurrentGauge() + reducedGauge, triggeringElement.getCurrentGauge() + reducedGauge);
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
			.info("Frozen (Elemental Application): {} | Frozen (Status Effect): {}", ElementComponent.KEY.get(entity).getElementalApplication(Element.FROZEN), entity.getStatusEffect(OriginsGenshinStatusEffects.FROZEN));
	}
}
