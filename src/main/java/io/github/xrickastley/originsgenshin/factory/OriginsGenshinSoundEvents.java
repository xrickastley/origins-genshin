package io.github.xrickastley.originsgenshin.factory;

import io.github.xrickastley.originsgenshin.OriginsGenshin;

import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;

public class OriginsGenshinSoundEvents {
	public static final SoundEvent REACTION = register("reaction");
	public static final SoundEvent DENDRO_CORE_EXPLOSION = register("dendro_core_explosion");
	public static final SoundEvent SPRAWLING_SHOT_HIT = register("sprawling_shot_hit");
	public static final SoundEvent CRYSTALLIZE_SHIELD = register("crystallize_shield");
	public static final SoundEvent CRYSTALLIZE_SHIELD_HIT = register("crystallize_shield.hit");
	public static final SoundEvent CRYSTALLIZE_SHIELD_BREAK = register("crystallize_shield.break");

	// Initializes the class upon call by OriginsGenshin.
	public static void register() {}

	private static SoundEvent register(String id) {
		return register(OriginsGenshin.identifier(id));
	}

	private static SoundEvent register(Identifier id) {
		return register(id, id);
	}

	private static SoundEvent register(Identifier id, Identifier soundId) {
		return Registry.register(Registries.SOUND_EVENT, id, SoundEvent.of(soundId));
	}
}
