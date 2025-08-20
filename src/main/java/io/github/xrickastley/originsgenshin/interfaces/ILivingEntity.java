package io.github.xrickastley.originsgenshin.interfaces;

import org.jetbrains.annotations.Nullable;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;

public interface ILivingEntity {
	/**
	 * Gets the "planned" attacker. This is updated at the <i>very</i> start of the damage method,
	 * meaning that the attacker may not be able to attack after all succeeding conditions are
	 * checked. <br> <br>
	 * 
	 * This is the attacker of the <i>most recent</i> {@code DamageSource} passed through
	 * {@link LivingEntity#damage LivingEntity#damage}.
	 * 
	 * @see LivingEntity#getAttacker()
	 */
	default @Nullable Entity originsgenshin$getPlannedAttacker() {
		return null;
	}
}
