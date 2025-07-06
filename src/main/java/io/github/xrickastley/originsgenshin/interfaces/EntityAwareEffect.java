package io.github.xrickastley.originsgenshin.interfaces;

import net.minecraft.entity.LivingEntity;

public interface EntityAwareEffect {
	default void onRemoved(LivingEntity entity, int amplifier) {}
}