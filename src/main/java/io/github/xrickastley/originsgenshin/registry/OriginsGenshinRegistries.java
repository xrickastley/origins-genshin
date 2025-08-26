package io.github.xrickastley.originsgenshin.registry;

import com.mojang.serialization.Lifecycle;

import io.github.xrickastley.originsgenshin.element.InternalCooldownType;
import io.github.xrickastley.originsgenshin.element.reaction.ElementalReaction;
import io.github.xrickastley.originsgenshin.registry.OriginsGenshinReloadListener.ReloadableRegistry;

import net.fabricmc.fabric.api.event.registry.FabricRegistryBuilder;
import net.fabricmc.fabric.api.event.registry.RegistryAttribute;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.SimpleRegistry;

public class OriginsGenshinRegistries {
	public static final Registry<ElementalReaction> ELEMENTAL_REACTION = createIntrusiveRegistry(OriginsGenshinRegistryKeys.ELEMENTAL_REACTION);
	public static final Registry<InternalCooldownType> INTERNAL_COOLDOWN_TYPE = createRegistry(OriginsGenshinRegistryKeys.INTERNAL_COOLDOWN_TYPE);

	public static void load() {};

	private static <T> Registry<T> createIntrusiveRegistry(RegistryKey<Registry<T>> registryKey) {
		return FabricRegistryBuilder
			.from(new SimpleRegistry<>(registryKey, Lifecycle.stable(), true))
			.attribute(RegistryAttribute.SYNCED)
			.buildAndRegister();
	}

	private static <T> Registry<T> createRegistry(RegistryKey<Registry<T>> registryKey) {
		return FabricRegistryBuilder
			.createSimple(registryKey)
			.buildAndRegister();
	}

	static {
		Registry.register(OriginsGenshinRegistries.INTERNAL_COOLDOWN_TYPE, InternalCooldownType.NONE.getId(), InternalCooldownType.NONE);
		Registry.register(OriginsGenshinRegistries.INTERNAL_COOLDOWN_TYPE, InternalCooldownType.DEFAULT.getId(), InternalCooldownType.DEFAULT);

		OriginsGenshinReloadListener.addReloadableRegistry(
			ReloadableRegistry.ofCodec(
				OriginsGenshinRegistries.INTERNAL_COOLDOWN_TYPE,
				InternalCooldownType.Builder::getInstance,
				InternalCooldownType.Builder.CODEC
			)
		);

		OriginsGenshinReloadListener.addBeforeLoadListener(
			OriginsGenshinRegistries.INTERNAL_COOLDOWN_TYPE,
			registry -> {
				Registry.register(registry, InternalCooldownType.NONE.getId(), InternalCooldownType.NONE);
				Registry.register(registry, InternalCooldownType.DEFAULT.getId(), InternalCooldownType.DEFAULT);
			}
		);

		OriginsGenshinReloadListener.addAfterLoadListener(
			OriginsGenshinRegistries.INTERNAL_COOLDOWN_TYPE,
			registry -> {
				
			}
		);
	}
}
