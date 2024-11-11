package io.github.xrickastley.originsgenshin.registry;

import io.github.xrickastley.originsgenshin.OriginsGenshin;
import io.github.xrickastley.originsgenshin.element.reaction.ElementalReaction;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;

public class OriginsGenshinRegistryKeys {
	public static final RegistryKey<Registry<ElementalReaction>> ELEMENTAL_REACTION = createRegistryKey("elemental_reaction");
	
	public static void load() {};

	private static <T> RegistryKey<Registry<T>> createRegistryKey(String path) {
		try {
			return RegistryKey.ofRegistry(OriginsGenshin.identifier(path));
		} catch (Exception e) {
			OriginsGenshin.LOGGER.error("An error occured while creating Registry keys!", e);

			return null;
		}
	}
}
