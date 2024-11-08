package io.github.xrickastley.originsgenshin.elements.reactions;

import com.google.common.collect.HashMultimap;

import io.github.xrickastley.originsgenshin.elements.ElementalApplication;
import io.github.xrickastley.originsgenshin.factories.OriginsGenshinAttributes;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributeModifier.Operation;

public abstract class AbstractSuperconductElementalReaction extends ElementalReaction {
	protected AbstractSuperconductElementalReaction(ElementalReactionSettings settings) {
		super(settings);
	}

	@Override
	protected void onReaction(LivingEntity entity, ElementalApplication auraElement, ElementalApplication triggeringElement, double reducedGauge) {
		final HashMultimap<EntityAttribute, EntityAttributeModifier> modifiers = HashMultimap.create();

		modifiers.put(OriginsGenshinAttributes.PHYSICAL_RES, new EntityAttributeModifier("superconduct", -0.4, Operation.ADDITION));

		entity
			.getAttributes()
			.addTemporaryModifiers(modifiers);
	}
}