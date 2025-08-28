package io.github.xrickastley.originsgenshin.factory;

import java.util.function.Supplier;

import io.github.apace100.apoli.power.factory.condition.ConditionFactory;
import io.github.apace100.apoli.registry.ApoliRegistries;
import io.github.xrickastley.originsgenshin.condition.damage.*;

import net.minecraft.entity.damage.DamageSource;
import net.minecraft.registry.Registry;
import net.minecraft.util.Pair;

public class OriginsGenshinDamageConditions {
	public static void register() {
		register(ElementCondition::getFactory);
	}

	private static ConditionFactory<Pair<DamageSource, Float>> register(Supplier<ConditionFactory<Pair<DamageSource, Float>>> conditionFactory) {
		return Registry.register(ApoliRegistries.DAMAGE_CONDITION, conditionFactory.get().getSerializerId(), conditionFactory.get());
	}
}
