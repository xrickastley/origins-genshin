package io.github.xrickastley.originsgenshin.factory;

import io.github.xrickastley.originsgenshin.element.reaction.ElementalReactions;
import io.github.xrickastley.originsgenshin.registry.OriginsGenshinRegistries;
import io.github.xrickastley.originsgenshin.registry.OriginsGenshinRegistryKeys;

public class OriginsGenshinFactories {
	public static void registerAll() {
		OriginsGenshinRegistryKeys.load();
		OriginsGenshinRegistries.load();

		OriginsGenshinAttributes.register();
		OriginsGenshinBiEntityActions.register();
		OriginsGenshinEntities.register();
		OriginsGenshinEntityActions.register();
		OriginsGenshinGameRules.register();
		OriginsGenshinPowers.register();
		OriginsGenshinSoundEvents.register();
		OriginsGenshinStatusEffects.register();
		
		ElementalReactions.register();
	}
}
