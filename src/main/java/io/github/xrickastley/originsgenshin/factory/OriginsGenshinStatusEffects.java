package io.github.xrickastley.originsgenshin.factory;

import io.github.xrickastley.originsgenshin.OriginsGenshin;
import io.github.xrickastley.originsgenshin.effect.FrozenStatusEffect;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

public class OriginsGenshinStatusEffects {
	/**
	 * Freezes the entity, preventing movement and attacks, as well as "disabling" their active powers.
	 */
	public static final StatusEffect FROZEN = new FrozenStatusEffect();	
	
	public static void register() {
		OriginsGenshinStatusEffects.register("frozen", FROZEN);
	}

	private static StatusEffect register(String name, StatusEffect statusEffect) {
		return Registry.register(Registries.STATUS_EFFECT, OriginsGenshin.identifier(name), statusEffect);
	}
}
