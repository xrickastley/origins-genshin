package io.github.xrickastley.originsgenshin.element;

import io.github.xrickastley.originsgenshin.OriginsGenshin;

/**
 * An {@code InternalCooldown} is a class used for holding the various {@code InternalCooldown}
 * components together in a single class. <br> <br>
 * 
 * In addition to this, this class is also the handler for the Internal Cooldown system. <br> <br>
 * 
 * When the Internal Cooldown is considered active through {@link InternalCooldown#isInInternalCooldown()},
 * elements should <b>not</b> be applied or refreshed. <br> <br>
 * 
 * To read more about the {@code InternalCooldown}, refer to the {@link InternalCooldownContext} class.
 */
public final class InternalCooldown {
	private final InternalCooldownHolder holder;
	private final InternalCooldownType type;
	private final InternalCooldownTag tag;
	private int cooldown = 0;
	private int totalHits = 0;

	public InternalCooldown(final InternalCooldownHolder holder, final InternalCooldownTag tag, final InternalCooldownType type) {
		this.holder = holder;
		this.type = type;
		this.tag = tag;
	}

	public static String getIcdIdentifier(InternalCooldownTag tag, InternalCooldownType type) {
		return tag.getTag() + type.getId().toString();
	}

	public static String getIcdIdentifier(String tag, InternalCooldownType type) {
		return tag + type.getId().toString();
	}
	
	/**
	 * Checks if an element can be applied. <br> <br>
	 * 
	 * @see {@link InternalCooldown#isInInternalCooldown} for registering a hit and checking the
	 * Internal Cooldown after.
	 */
	public boolean isInInternalCooldown() {
		return tag != null && holder.getOwner().age >= cooldown || totalHits > type.getGaugeSequence();
	}

	/**
	 * Handles the Internal Cooldown. <br> <br>
	 * 
	 * Upon using this method, a hit is registered, and it returns whether or not the element can
	 * be applied. <br> <br>
	 * 
	 * @see {@link InternalCooldown#isInInternalCooldown} for only checking the Internal Cooldown
	 * without registering a hit.
	 */
	public boolean handleInternalCooldown() {
		if (tag == null) return true;
		
		OriginsGenshin
			.sublogger(this)
			.info("InternalCooldown@{} => Reset Interval ({} ticks): {} ({} ≥ {}) | Gauge Sequence ({}-hit rule): {} ({} ≥ 3)", Integer.toHexString(this.hashCode()), type.getResetInterval(), holder.getOwner().age >= cooldown, holder.getOwner().age, cooldown, type.getGaugeSequence(), totalHits >= type.getGaugeSequence(), totalHits, type.getGaugeSequence());
		
		if (holder.getOwner().age >= cooldown) {
			cooldown = holder.getOwner().age + type.getResetInterval();
			totalHits = 1;

			return true;
		} else if (totalHits >= type.getGaugeSequence()) {
			totalHits = 1;
			
			return true;
		} else {
			totalHits += 1;

			return false;
		}
	}
}
