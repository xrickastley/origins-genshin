package io.github.xrickastley.originsgenshin.factory;

import io.github.apace100.apoli.power.factory.action.ActionFactory;
import io.github.apace100.apoli.registry.ApoliRegistries;
import io.github.xrickastley.originsgenshin.action.bientity.ElementalDamageAction;
import net.minecraft.entity.Entity;
import net.minecraft.registry.Registry;
import net.minecraft.util.Pair;

public class OriginsGenshinBiEntityActions {
	public static void register() {
		register(ElementalDamageAction.getFactory());
	}

	private static ActionFactory<Pair<Entity, Entity>> register(ActionFactory<Pair<Entity, Entity>> actionFactory) {
		return Registry.register(ApoliRegistries.BIENTITY_ACTION, actionFactory.getSerializerId(), actionFactory);
	}
}
