package io.github.xrickastley.originsgenshin.factory;

import java.util.function.Supplier;

import io.github.apace100.apoli.power.factory.condition.ConditionFactory;
import io.github.apace100.apoli.registry.ApoliRegistries;
import io.github.xrickastley.originsgenshin.condition.entity.*;

import net.minecraft.entity.Entity;
import net.minecraft.registry.Registry;

public class OriginsGenshinEntityConditions {
	public static void register() {
		register(HasElementCondition::getFactory);
	}

	private static ConditionFactory<Entity> register(Supplier<ConditionFactory<Entity>> conditionFactory) {
		return Registry.register(ApoliRegistries.ENTITY_CONDITION, conditionFactory.get().getSerializerId(), conditionFactory.get());
	}
}
