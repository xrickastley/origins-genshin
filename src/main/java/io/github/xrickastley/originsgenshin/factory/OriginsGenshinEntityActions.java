package io.github.xrickastley.originsgenshin.factory;

import java.util.function.Supplier;

import io.github.apace100.apoli.power.factory.action.ActionFactory;
import io.github.apace100.apoli.registry.ApoliRegistries;
import io.github.xrickastley.originsgenshin.action.entity.*;

import net.minecraft.entity.Entity;
import net.minecraft.registry.Registry;

public class OriginsGenshinEntityActions {
	public static void register() {
		register(ApplyElementAction::getFactory);
		register(ElementalDamageAction::getFactory);
	}

	private static ActionFactory<Entity> register(Supplier<ActionFactory<Entity>> actionFactory) {
		return Registry.register(ApoliRegistries.ENTITY_ACTION, actionFactory.get().getSerializerId(), actionFactory.get());
	}
}
