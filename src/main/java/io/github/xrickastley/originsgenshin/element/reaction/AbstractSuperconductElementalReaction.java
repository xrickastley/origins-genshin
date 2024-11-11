package io.github.xrickastley.originsgenshin.element.reaction;

import javax.annotation.Nullable;

import com.google.common.collect.HashMultimap;

import io.github.xrickastley.originsgenshin.OriginsGenshin;
import io.github.xrickastley.originsgenshin.element.ElementalApplication;
import io.github.xrickastley.originsgenshin.factory.OriginsGenshinAttributes;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributeModifier.Operation;

public abstract class AbstractSuperconductElementalReaction extends ElementalReaction {
	protected AbstractSuperconductElementalReaction(ElementalReactionSettings settings) {
		super(settings);
	}

	@Override
	protected void onReaction(LivingEntity entity, ElementalApplication auraElement, ElementalApplication triggeringElement, double reducedGauge, @Nullable LivingEntity origin) {
		final HashMultimap<EntityAttribute, EntityAttributeModifier> modifiers = HashMultimap.create();

		modifiers.put(OriginsGenshinAttributes.PHYSICAL_RES, new EntityAttributeModifier("superconduct", -0.4, Operation.ADDITION));

		entity
			.getAttributes()
			.addTemporaryModifiers(modifiers);

		OriginsGenshin.SCHEDULER.queue((server -> {
			entity
				.getAttributes()
				.removeModifiers(modifiers);
		}), 12 * 20);
	}
}