package io.github.xrickastley.originsgenshin.factory;

import io.github.xrickastley.originsgenshin.OriginsGenshin;
import io.github.xrickastley.originsgenshin.effect.CryoStatusEffect;
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
	 * Reduces the entity's Physical RES% by 40%.
	 */
	public static final StatusEffect SUPERCONDUCT = new SuperconductStatusEffect();
	/**
	 * Reduces the entity's Movement Speed and Attack Speed by 15%.
	 */
	public static final StatusEffect CRYO = new CryoStatusEffect();

	public static void register() {
		OriginsGenshinStatusEffects.register("frozen", FROZEN);
		OriginsGenshinStatusEffects.register("superconduct", SUPERCONDUCT);
		OriginsGenshinStatusEffects.register("cryo", CRYO);
	}

	private static StatusEffect register(String name, StatusEffect statusEffect) {
		return Registry.register(Registries.STATUS_EFFECT, OriginsGenshin.identifier(name), statusEffect);
	}
}
