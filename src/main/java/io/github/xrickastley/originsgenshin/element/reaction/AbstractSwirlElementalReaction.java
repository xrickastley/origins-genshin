package io.github.xrickastley.originsgenshin.element.reaction;

import javax.annotation.Nullable;

import io.github.xrickastley.originsgenshin.element.Element;
import io.github.xrickastley.originsgenshin.element.ElementalApplication;
import io.github.xrickastley.originsgenshin.element.ElementalDamageSource;
import io.github.xrickastley.originsgenshin.element.InternalCooldownContext;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageTypes;

public abstract class AbstractSwirlElementalReaction extends ElementalReaction {
	private final Element swirlElement;

	/**
	 * Creates a Swirl reaction with the specified settings. <br> <br>
	 * 
	 * The specified <b>aura element</b> will serve as the "swirlable" element. <br> <br> 
	 * 
	 * For example, if the Aura Element is {@link Element#PYRO}, then the Pyro element is swirled
	 * and spread to nearby targets (r=3m). <br> <br>
	 * 
	 * For the Gauge Units applied by the Swirl reaction, as well as it's duration, you may refer
	 * here: <a href=https://genshin-impact.fandom.com/wiki/Elemental_Gauge_Theory/Advanced_Mechanics#Swirl_Elemental_Application">
	 * Swirl Elemental Application</a> 
	 * @param settings The {@code ElementalReactionSettings} for this {@code ElementalReaction}.
	 */
	protected AbstractSwirlElementalReaction(ElementalReactionSettings settings) {
		super(settings);

		this.swirlElement = settings.auraElement.getLeft();
	}

	@Override
	protected void onReaction(LivingEntity entity, ElementalApplication auraElement, ElementalApplication triggeringElement, double reducedGauge, @Nullable LivingEntity origin) {
		final double gaugeOriginAura = auraElement.getCurrentGauge() + reducedGauge;
		final double gaugeAnemo = triggeringElement.getCurrentGauge() + reducedGauge;

		final double gaugeReaction = gaugeOriginAura >= (0.5 * gaugeAnemo)
			? gaugeAnemo
			: gaugeOriginAura;

		final double gaugeSwirlAttack = ((gaugeReaction - 0.04) * 1.25) + 1;

		for (final LivingEntity target  : ElementalReaction.getEntitiesInAoE(entity, 3, t -> t != origin)) {
			final float damage = ElementalReaction.getReactionDamage(entity, 0.6);
			final ElementalDamageSource source = new ElementalDamageSource(
				entity
					.getDamageSources()
					.create(DamageTypes.PLAYER_ATTACK, origin),
				ElementalApplication.gaugeUnits(target, swirlElement, gaugeSwirlAttack, true),
				InternalCooldownContext.ofNone(origin)
			);

			target.damage(source, damage);
		}
	}
}