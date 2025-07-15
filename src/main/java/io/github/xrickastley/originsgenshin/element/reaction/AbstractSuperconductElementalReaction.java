package io.github.xrickastley.originsgenshin.element.reaction;

import javax.annotation.Nullable;

import io.github.xrickastley.originsgenshin.element.Element;
import io.github.xrickastley.originsgenshin.element.ElementalApplication;
import io.github.xrickastley.originsgenshin.element.ElementalDamageSource;
import io.github.xrickastley.originsgenshin.element.InternalCooldownContext;
import io.github.xrickastley.originsgenshin.factory.OriginsGenshinStatusEffects;
import io.github.xrickastley.originsgenshin.registry.OriginsGenshinDamageTypes;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;

public abstract sealed class AbstractSuperconductElementalReaction 
	extends ElementalReaction
	permits SuperconductElementalReaction, FrozenSuperconductElementalReaction
{
	protected AbstractSuperconductElementalReaction(ElementalReactionSettings settings) {
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
				ElementalApplication.gaugeUnits(target, Element.CRYO, 1.5),
				InternalCooldownContext.ofNone(origin)
			);

			target.damage(source, damage);
			target.addStatusEffect(new StatusEffectInstance(OriginsGenshinStatusEffects.SUPERCONDUCT, 240, 1), origin);
		}
	}
}