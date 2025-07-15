package io.github.xrickastley.originsgenshin.element.reaction;

import javax.annotation.Nullable;

import io.github.xrickastley.originsgenshin.element.Element;
import io.github.xrickastley.originsgenshin.element.ElementalApplication;
import io.github.xrickastley.originsgenshin.element.ElementalDamageSource;
import io.github.xrickastley.originsgenshin.element.InternalCooldownContext;
import io.github.xrickastley.originsgenshin.registry.OriginsGenshinDamageTypes;
import net.minecraft.entity.LivingEntity;

public abstract sealed class AbstractSwirlElementalReaction 
	extends ElementalReaction 
	permits PyroSwirlElementalReaction, HydroSwirlElementalReaction, ElectroSwirlElementalReaction, CryoSwirlElementalReaction, FrozenSwirlElementalReaction
{
	private final Element swirlElement;
	private final boolean damageTargetInstead;

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
	 * 
	 * @param settings The {@code ElementalReactionSettings} for this {@code ElementalReaction}.
	 */
	protected AbstractSwirlElementalReaction(ElementalReactionSettings settings) {
		this(settings, settings.auraElement.getLeft());
	}

	/**
	 * Creates a Swirl reaction with the specified settings. <br> <br>
	 * 
	 * The "swirlable" element is the spread element upon triggering the swirl reaction. <br> <br> 
	 * 
	 * For example, if the swirlable Element is {@link Element#PYRO}, then the Pyro element is 
	 * swirled and spread to nearby targets (r=3m). <br> <br>
	 * 
	 * For the Gauge Units applied by the Swirl reaction, as well as it's duration, you may refer
	 * here: <a href=https://genshin-impact.fandom.com/wiki/Elemental_Gauge_Theory/Advanced_Mechanics#Swirl_Elemental_Application">
	 * Swirl Elemental Application</a> 
	 * 
	 * @param settings The {@code ElementalReactionSettings} for this {@code ElementalReaction}.
	 * @param damageTargetInstead Whether or not Swirl will <i>only</i> spread the element and 
	 * deals damage to the Swirl target instead, i.e. the entity the Swirl reaction was triggered
	 * on.
	 */
	protected AbstractSwirlElementalReaction(ElementalReactionSettings settings, boolean damageTargetInstead) {
		super(settings);

		this.swirlElement = settings.auraElement.getLeft();
		this.damageTargetInstead = damageTargetInstead;
	}

	/**
	 * Creates a Swirl reaction with the specified settings. <br> <br>
	 * 
	 * The "swirlable" element is the spread element upon triggering the swirl reaction. <br> <br> 
	 * 
	 * For example, if the swirlable Element is {@link Element#PYRO}, then the Pyro element is 
	 * swirled and spread to nearby targets (r=3m). <br> <br>
	 * 
	 * For the Gauge Units applied by the Swirl reaction, as well as it's duration, you may refer
	 * here: <a href=https://genshin-impact.fandom.com/wiki/Elemental_Gauge_Theory/Advanced_Mechanics#Swirl_Elemental_Application">
	 * Swirl Elemental Application</a> 
	 * 
	 * @param settings The {@code ElementalReactionSettings} for this {@code ElementalReaction}.
	 * @param swirlElement The element to Swirl.
	 */
	protected AbstractSwirlElementalReaction(ElementalReactionSettings settings, Element swirlElement) {
		super(settings);

		this.swirlElement = swirlElement;
		this.damageTargetInstead = false;
	}

	@Override
	protected void onReaction(LivingEntity entity, ElementalApplication auraElement, ElementalApplication triggeringElement, double reducedGauge, @Nullable LivingEntity origin) {
		final double gaugeOriginAura = auraElement.getCurrentGauge() + reducedGauge;
		final double gaugeAnemo = triggeringElement.getCurrentGauge() + reducedGauge;

		final double gaugeReaction = gaugeOriginAura >= (0.5 * gaugeAnemo)
			? gaugeAnemo
			: gaugeOriginAura;

		final double gaugeSwirlAttack = ((gaugeReaction - 0.04) * 1.25) + 1;

		for (final LivingEntity target : ElementalReaction.getEntitiesInAoE(entity, 6, t -> t != origin)) {			
			// Simplification of (damageTargetInstead && target == entity) || (!damageTargetInstead && target != entity), an XNOR
			final float damage = damageTargetInstead == (target == entity) 
				? ElementalReaction.getReactionDamage(entity, 0.6) 
				: 0f;
			
			final ElementalDamageSource source = new ElementalDamageSource(
				entity
					.getDamageSources()
					.create(OriginsGenshinDamageTypes.SWIRL, origin),
				ElementalApplication.gaugeUnits(target, swirlElement, target == entity ? 0f : gaugeSwirlAttack, true),
				InternalCooldownContext.ofNone(origin)
			);

			target.damage(source, damage);
		}
	}
}