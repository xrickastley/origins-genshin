package io.github.xrickastley.originsgenshin.element.reaction;

import javax.annotation.Nullable;

import io.github.xrickastley.originsgenshin.OriginsGenshin;
import io.github.xrickastley.originsgenshin.component.ElementComponent;
import io.github.xrickastley.originsgenshin.element.Element;
import io.github.xrickastley.originsgenshin.element.ElementalApplication;
import io.github.xrickastley.originsgenshin.factory.OriginsGenshinParticleFactory;
import io.github.xrickastley.originsgenshin.factory.OriginsGenshinStatusEffects;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;

public final class FrozenElementalReaction extends ElementalReaction {
	public FrozenElementalReaction() {
		super(
			new ElementalReactionSettings("frozen", OriginsGenshin.identifier("frozen"), OriginsGenshinParticleFactory.Frozen)
				// Triggering Frozen should consume the entirety of both Cryo and Hydro aura.
				.setReactionCoefficient(Double.MAX_VALUE)
				.setAuraElement(Element.CRYO, 4)
				.setTriggeringElement(Element.HYDRO, 3)
				.setAsReversable(true)
		);
	}

	@Override
	protected void onReaction(LivingEntity entity, ElementalApplication auraElement, ElementalApplication triggeringElement, double reducedGauge, @Nullable LivingEntity origin) {
		// Gauge-FreezeAura = 2 * min(Gauge-OriginAura, Gauge-TriggerElement)
		final double freezeAuraGauge = 2 * Math.min(auraElement.getCurrentGauge() + reducedGauge, triggeringElement.getCurrentGauge() + reducedGauge);
		// Freeze Duration (Seconds) = 2√(5 * freezeAuraGauge) + 4) - 4
		final float freezeDuration = 2f * (float) Math.sqrt((5 * freezeAuraGauge) + 4) - 4;

		entity.addStatusEffect(
			new StatusEffectInstance(OriginsGenshinStatusEffects.FROZEN, (int) Math.floor(freezeDuration * 20))
		);

		entity
			.getServer()
			.execute(() -> {
				ElementComponent.KEY
					.get(entity)
					.addElementalApplication(
						ElementalApplication.usingDuration(entity, Element.FROZEN, freezeAuraGauge, freezeDuration),
						"reactions:frozen"
					);
			});
	}
}
