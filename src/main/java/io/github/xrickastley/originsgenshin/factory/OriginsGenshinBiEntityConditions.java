package io.github.xrickastley.originsgenshin.factory;

import java.util.function.Supplier;

import io.github.apace100.apoli.power.factory.condition.ConditionFactory;
import io.github.apace100.apoli.registry.ApoliRegistries;
import io.github.xrickastley.originsgenshin.condition.bientity.*;
import net.minecraft.entity.Entity;
import net.minecraft.registry.Registry;
import net.minecraft.util.Pair;

public class OriginsGenshinBiEntityConditions {
	public static void register() {
		register(InInternalCooldownCondition::getFactory);
	}

	private static ConditionFactory<Pair<Entity, Entity>> register(Supplier<ConditionFactory<Pair<Entity, Entity>>> conditionFactory) {
		return Registry.register(ApoliRegistries.BIENTITY_CONDITION, conditionFactory.get().getSerializerId(), conditionFactory.get());
	}
}
