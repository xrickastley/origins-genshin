package io.github.botcoder69.originsgenshin.interfaces;

import io.github.apace100.apoli.power.ActiveCooldownPower;

import net.minecraft.entity.player.PlayerEntity;

public interface IOrigin {
	public boolean hasElementalBurstPower(PlayerEntity entity);
	public ActiveCooldownPower getElementalBurstPower(PlayerEntity entity);
	public boolean hasElementalSkillPower(PlayerEntity entity);
	public ActiveCooldownPower getElementalSkillPower(PlayerEntity entity);
}