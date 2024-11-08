package io.github.xrickastley.originsgenshin.elements;

import net.minecraft.entity.LivingEntity;

public final class InternalCooldownData {
	private final LivingEntity owner;
	private int cooldown = 0;
	private int totalHits = 0;

	private InternalCooldownData(LivingEntity owner) {
		this.owner = owner;
	}

	public static InternalCooldownData blank(LivingEntity owner) {
		return new InternalCooldownData(owner);
	}

	/**
	 * Checks if an element can be applied. <br> <br>
	 * 
	 * If you want to register a hit, then check the Internal Cooldown, use {@link InternalCooldownData#handleInternalCooldown} instead.
	 */
	public boolean inInternalCooldown() {
		return owner.age >= cooldown || totalHits > 3;
	}

	/**
	 * Handles the Internal Cooldown. If you use this method, a hit is counted. Returns whether or not the element can be applied. <br> <br>
	 * 
	 * If you want to only check the Internal Cooldown, but not register a hit, use {@link InternalCooldownData#inInternalCooldown} instead.
	 */
	public boolean handleInternalCooldown() {
		System.out.printf("%s => 2.5s cooldown: %b (%d > %d) || 3-hit rule: %b (%d > 3)\n", this.toString(), owner.age >= cooldown, owner.age, cooldown, totalHits > 3, totalHits);
		
		if (owner.age >= cooldown) {
			cooldown = owner.age + 50;
			totalHits = 0;

			return true;
		} else if (totalHits > 3) {
			totalHits = 0;
			
			return true;
		} else {
			totalHits += 1;

			return false;
		}
	}
}
