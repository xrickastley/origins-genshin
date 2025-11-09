package io.github.xrickastley.originsgenshin.factory;

import java.util.function.Supplier;

import io.github.apace100.apoli.power.factory.PowerFactory;
import io.github.apace100.apoli.registry.ApoliRegistries;
import io.github.xrickastley.originsgenshin.power.ActionOnElementAppliedPower;
import io.github.xrickastley.originsgenshin.power.ActionOnElementReappliedPower;
import io.github.xrickastley.originsgenshin.power.ActionOnElementRefreshedPower;
import io.github.xrickastley.originsgenshin.power.ActionOnElementRemovedPower;
import io.github.xrickastley.originsgenshin.power.ActionOnElementalReactionPower;
import io.github.xrickastley.originsgenshin.power.ElementalInfusionPower;

import net.minecraft.registry.Registry;

public class OriginsGenshinPowers {
	public static void register() {
		register(ActionOnElementalReactionPower::createFactory);
		register(ActionOnElementAppliedPower::createFactory);
		register(ActionOnElementReappliedPower::createFactory);
		register(ActionOnElementRefreshedPower::createFactory);
		register(ActionOnElementRemovedPower::createFactory);
		register(ElementalInfusionPower::createFactory);
	}

	private static PowerFactory<?> register(Supplier<PowerFactory<?>> supplier) {
		return Registry.register(ApoliRegistries.POWER_FACTORY, supplier.get().getSerializerId(), supplier.get());
	}
}
