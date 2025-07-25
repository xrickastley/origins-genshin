package io.github.xrickastley.originsgenshin.factory;

import io.github.xrickastley.originsgenshin.OriginsGenshin;
import io.github.xrickastley.originsgenshin.effect.FrozenStatusEffect;
import io.github.xrickastley.originsgenshin.effect.SuperconductStatusEffect;

import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

public class OriginsGenshinStatusEffects {
	/**
	 * Freezes the entity, preventing movement and attacks, as well as "disabling" their active powers.
	 */
	public static final StatusEffect FROZEN = new FrozenStatusEffect();
	/**
	 * Applies a -40% Physical RES% onto the entity.
	 */
	public static final StatusEffect SUPERCONDUCT = new SuperconductStatusEffect();

	public static void register() {
		OriginsGenshinStatusEffects.register("frozen", FROZEN);
		OriginsGenshinStatusEffects.register("superconduct", SUPERCONDUCT);
	}

	private static StatusEffect register(String name, StatusEffect statusEffect) {
		return Registry.register(Registries.STATUS_EFFECT, OriginsGenshin.identifier(name), statusEffect);
	}
}
