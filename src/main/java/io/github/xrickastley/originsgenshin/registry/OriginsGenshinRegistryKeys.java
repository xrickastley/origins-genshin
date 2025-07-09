package io.github.xrickastley.originsgenshin.registry;

import io.github.xrickastley.originsgenshin.OriginsGenshin;
import io.github.xrickastley.originsgenshin.element.InternalCooldownType;
import io.github.xrickastley.originsgenshin.element.reaction.ElementalReaction;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;	

public class OriginsGenshinRegistryKeys {
	public static final RegistryKey<Registry<ElementalReaction>> ELEMENTAL_REACTION = createRegistryKey("elemental_reaction");
	public static final RegistryKey<Registry<InternalCooldownType>> INTERNAL_COOLDOWN_TYPE = createRegistryKey("internal_cooldowns");
	
	public static void load() {};

	private static <T> RegistryKey<Registry<T>> createRegistryKey(String path) {
		return RegistryKey.ofRegistry(OriginsGenshin.identifier(path));
	}
}
