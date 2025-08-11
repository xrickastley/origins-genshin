package io.github.xrickastley.originsgenshin.element.reaction;

import io.github.xrickastley.originsgenshin.OriginsGenshin;
import io.github.xrickastley.originsgenshin.element.Element;
import io.github.xrickastley.originsgenshin.element.ElementalApplication;
import io.github.xrickastley.originsgenshin.element.ElementalApplications;
import io.github.xrickastley.originsgenshin.element.ElementalDamageSource;
import io.github.xrickastley.originsgenshin.element.InternalCooldownContext;
import io.github.xrickastley.originsgenshin.factory.OriginsGenshinStatusEffects;
import io.github.xrickastley.originsgenshin.registry.OriginsGenshinDamageTypes;

import net.minecraft.entity.LivingEntity;

import javax.annotation.Nullable;

/*
 * DEV NOTE: No concept of "Poise" exists within Origins: Genshin, therefore the implementation of
 * Shatter has been modified slightly.
 *
 * When the target takes **any** Geo DMG (considering ICD), Shatter is triggered and the Frozen Aura
 * is removed.
 */
public final class ShatterElementalReaction extends ElementalReaction {
	ShatterElementalReaction() {
		super(
			new ElementalReactionSettings("Shatter", OriginsGenshin.identifier("shatter"), null)
				.setReactionCoefficient(0)
				.setAuraElement(Element.FROZEN)
				.setTriggeringElement(Element.GEO, 1)
		);
	}

	@Override
	protected void onReaction(LivingEntity entity, ElementalApplication auraElement, ElementalApplication triggeringElement, double reducedGauge, @Nullable LivingEntity origin) {
		auraElement.reduceGauge(Double.MAX_VALUE);

		final float damage = ElementalReaction.getReactionDamage(entity, 3.0);
		final ElementalDamageSource source = new ElementalDamageSource(
			entity
				.getDamageSources()
				.create(OriginsGenshinDamageTypes.SHATTER, origin),
			ElementalApplications.gaugeUnits(entity, Element.PHYSICAL, 0.0, false),
			InternalCooldownContext.ofNone(entity)
		).shouldApplyDMGBonus(false);

		entity.damage(source, damage);
		entity.removeStatusEffect(OriginsGenshinStatusEffects.FROZEN);
	}
}
