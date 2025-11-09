package io.github.xrickastley.originsgenshin.factory;

public class OriginsGenshinFactories {
	public static void registerAll() {
		OriginsGenshinBiEntityActions.register();
		OriginsGenshinBiEntityConditions.register();
		OriginsGenshinDamageConditions.register();
		OriginsGenshinEntityActions.register();
		OriginsGenshinEntityConditions.register();
		OriginsGenshinPowers.register();
	}
}
