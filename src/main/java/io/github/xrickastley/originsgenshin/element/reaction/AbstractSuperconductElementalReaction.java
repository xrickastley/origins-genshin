package io.github.xrickastley.originsgenshin.element.reaction;

import javax.annotation.Nullable;

import com.google.common.collect.HashMultimap;

import io.github.xrickastley.originsgenshin.element.ElementalApplication;
import io.github.xrickastley.originsgenshin.factory.OriginsGenshinAttributes;
import io.github.xrickastley.originsgenshin.factory.OriginsGenshinStatusEffects;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributeModifier.Operation;
import net.minecraft.entity.effect.StatusEffectInstance;

public abstract class AbstractSuperconductElementalReaction extends ElementalReaction {
	protected AbstractSuperconductElementalReaction(ElementalReactionSettings settings) {
		super(settings);
	}

	@Override
	protected void onReaction(LivingEntity entity, ElementalApplication auraElement, ElementalApplication triggeringElement, double reducedGauge, @Nullable LivingEntity origin) {
		final HashMultimap<EntityAttribute, EntityAttributeModifier> modifiers = HashMultimap.create();

		modifiers.put(OriginsGenshinAttributes.PHYSICAL_RES, new EntityAttributeModifier("superconduct", -0.4, Operation.ADDITION));

		entity.addStatusEffect(new StatusEffectInstance(OriginsGenshinStatusEffects.SUPERCONDUCT, 240, 1), origin);
	}
}