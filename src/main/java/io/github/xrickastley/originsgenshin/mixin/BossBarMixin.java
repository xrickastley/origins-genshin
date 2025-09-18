package io.github.xrickastley.originsgenshin.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import io.github.xrickastley.originsgenshin.interfaces.IBossBar;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.boss.BossBar;

@Mixin(BossBar.class)
public abstract class BossBarMixin implements IBossBar {
	@Unique
	protected LivingEntity originsgenshin$entity;

	@Unique
	public void originsgenshin$setEntity(LivingEntity entity) {
		this.originsgenshin$entity = entity;
	}

	@Unique
	public LivingEntity originsgenshin$getEntity() {
		return this.originsgenshin$entity;
	}
}
