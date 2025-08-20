package io.github.xrickastley.originsgenshin.interfaces;

import net.minecraft.entity.damage.DamageSource;

public interface IPlayerEntity {
	/**
	 * Returns if the provided {@code DamageSource} corresponds to a "critical hit" from this
	 * {@code PlayerEntity}.
	 * 
	 * @param source The {@code DamageSource} to test.
	 */
	default boolean originsgenshin$isCrit(DamageSource source) {
		return false;
	}
}
