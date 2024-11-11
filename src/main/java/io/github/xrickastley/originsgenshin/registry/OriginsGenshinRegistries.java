package io.github.xrickastley.originsgenshin.registry;

import io.github.xrickastley.originsgenshin.OriginsGenshin;
import io.github.xrickastley.originsgenshin.element.reaction.ElementalReaction;
import net.fabricmc.fabric.api.event.registry.FabricRegistryBuilder;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;

public class OriginsGenshinRegistries {
	public static final Registry<ElementalReaction> ELEMENTAL_REACTION = createRegistry(OriginsGenshinRegistryKeys.ELEMENTAL_REACTION);

	public static void load() {};

	private static <T> Registry<T> createRegistry(RegistryKey<Registry<T>> registryKey) {
		try {
			return FabricRegistryBuilder
				.createSimple(registryKey)
				.buildAndRegister();
		} catch (Exception e) {
			OriginsGenshin.LOGGER.error("An error occured while creating Registries!", e);
			return null;
		}
	}
}
