package io.github.xrickastley.originsgenshin.element.reaction;

import io.github.xrickastley.originsgenshin.component.ElementComponent;
import io.github.xrickastley.originsgenshin.element.Element;
import io.github.xrickastley.originsgenshin.element.ElementalApplication;
import io.github.xrickastley.originsgenshin.element.ElementalApplications;
import io.github.xrickastley.originsgenshin.element.ElementalDamageSource;
import io.github.xrickastley.originsgenshin.element.InternalCooldownContext;
import io.github.xrickastley.originsgenshin.events.ReactionTriggered;
import io.github.xrickastley.originsgenshin.factory.OriginsGenshinSoundEvents;
import io.github.xrickastley.originsgenshin.factory.OriginsGenshinStatusEffects;
import io.github.xrickastley.originsgenshin.registry.OriginsGenshinDamageTypes;

import net.minecraft.entity.LivingEntity;
import net.minecraft.sound.SoundCategory;

import javax.annotation.Nullable;

/*
 * DEV NOTE: No concept of "Poise" exists within Origins: Genshin, therefore the implementation of
 * Shatter has been modified slightly.
 *
 * When the target takes **any** Geo DMG (considering ICD) or when they are hit with a Pickaxe or
 * an Axe, Shatter is triggered and the Frozen Aura is removed.
 */
public abstract sealed class ShatterElementalReaction
	extends ElementalReaction
	permits GeoShatterElementalReaction, HeavyShatterElementalReaction
{
	ShatterElementalReaction(ElementalReactionSettings settings) {
		super(settings);
	}

	@Override
	public boolean trigger(LivingEntity entity, @Nullable LivingEntity origin) {
		if (!isTriggerable(entity)) return false;

		final ElementComponent component = ElementComponent.KEY.get(entity);
		final ElementalApplication auraElement = component.getElementalApplication(this.auraElement.getLeft());
		final ElementalApplication triggeringElement = component.getElementalApplication(this.triggeringElement.getLeft());

		final double reducedGauge = auraElement.reduceGauge(Double.MAX_VALUE);

		this.onReaction(entity, auraElement, triggeringElement, reducedGauge, origin);
		this.displayReaction(entity);

		ReactionTriggered.EVENT
			.invoker()
			.onReactionTriggered(this, reducedGauge, entity, origin);

		entity
			.getWorld()
			.playSound(null, entity.getBlockPos(), OriginsGenshinSoundEvents.REACTION, SoundCategory.PLAYERS, 1.0f, 1.0f);

		return true;
	}

	@Override
	protected void onReaction(LivingEntity entity, ElementalApplication auraElement, ElementalApplication triggeringElement, double reducedGauge, @Nullable LivingEntity origin) {
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
