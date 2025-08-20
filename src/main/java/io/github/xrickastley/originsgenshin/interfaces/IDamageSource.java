package io.github.xrickastley.originsgenshin.interfaces;

public interface IDamageSource {
	/**
	 * Sets whether the DMG text should be displayed or not.
	 * 
	 * @param display Whether the DMG text should be displayed or not.
	 */
	default void originsgenshin$shouldDisplayDamage(boolean display) {}

	/**
	 * Whether the DMG text should be displayed or not.
	 */
	default boolean originsgenshin$displayDamage() {
		return true;
	}
}
