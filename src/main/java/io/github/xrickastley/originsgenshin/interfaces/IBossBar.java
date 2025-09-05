package io.github.xrickastley.originsgenshin.interfaces;

import org.jetbrains.annotations.Nullable;

import net.minecraft.entity.LivingEntity;

public interface IBossBar {
	default void originsgenshin$setEntity(@Nullable LivingEntity entity) {}

	default @Nullable LivingEntity originsgenshin$getEntity() {
		return null;
	}
}
