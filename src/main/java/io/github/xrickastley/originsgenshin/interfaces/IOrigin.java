package io.github.xrickastley.originsgenshin.interfaces;

import io.github.apace100.apoli.power.ActiveCooldownPower;

import net.minecraft.entity.player.PlayerEntity;

public interface IOrigin {
	public boolean originsgenshin$hasElementalBurstPower(PlayerEntity entity);
	public ActiveCooldownPower originsgenshin$getElementalBurstPower(PlayerEntity entity);
	public boolean originsgenshin$hasElementalSkillPower(PlayerEntity entity);
	public ActiveCooldownPower originsgenshin$getElementalSkillPower(PlayerEntity entity);
}
