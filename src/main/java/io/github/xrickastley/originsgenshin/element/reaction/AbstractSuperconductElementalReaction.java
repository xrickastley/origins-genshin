package io.github.xrickastley.originsgenshin.element.reaction;

import io.github.xrickastley.originsgenshin.element.Element;
import io.github.xrickastley.originsgenshin.element.ElementalApplication;
import io.github.xrickastley.originsgenshin.element.ElementalApplications;
import io.github.xrickastley.originsgenshin.element.ElementalDamageSource;
import io.github.xrickastley.originsgenshin.element.InternalCooldownContext;
import io.github.xrickastley.originsgenshin.factory.OriginsGenshinStatusEffects;
import io.github.xrickastley.originsgenshin.registry.OriginsGenshinDamageTypes;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;

import javax.annotation.Nullable;

public abstract sealed class AbstractSuperconductElementalReaction
	extends ElementalReaction
	permits SuperconductElementalReaction, FrozenSuperconductElementalReaction
{
	AbstractSuperconductElementalReaction(ElementalReactionSettings settings) {
		super(settings);
	}

	@Override
	protected void onReaction(LivingEntity entity, ElementalApplication auraElement, ElementalApplication triggeringElement, double reducedGauge, @Nullable LivingEntity origin) {
		for (final LivingEntity target  : ElementalReaction.getEntitiesInAoE(entity, 3, t -> t != origin)) {
			final float damage = ElementalReaction.getReactionDamage(entity, 1.5);
			final ElementalDamageSource source = new ElementalDamageSource(
				entity
					.getDamageSources()
					.create(OriginsGenshinDamageTypes.SUPERCONDUCT, origin),
				ElementalApplications.gaugeUnits(target, Element.CRYO, 0),
				InternalCooldownContext.ofNone(origin)
			).shouldApplyDMGBonus(false);

			target.damage(source, damage);
			target.addStatusEffect(new StatusEffectInstance(OriginsGenshinStatusEffects.SUPERCONDUCT, 240, 0), origin);
		}
	}
}