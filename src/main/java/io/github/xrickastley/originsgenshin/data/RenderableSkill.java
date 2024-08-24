package io.github.xrickastley.originsgenshin.data;

import java.util.List;

import io.github.apace100.apoli.power.ActiveCooldownPower;
import io.github.apace100.apoli.power.factory.condition.ConditionFactory;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;

public abstract class RenderableSkill {
	protected ActiveCooldownPower power;
	protected final boolean showCooldown;
	protected final boolean shouldRender; 
	protected final List<RenderableIcon> icons;
	protected final ConditionFactory<Entity>.Instance disable;

	public RenderableSkill(boolean showCooldown, boolean shouldRender, ConditionFactory<Entity>.Instance disableCondition, List<RenderableIcon> iconConditions) {
		this.showCooldown = showCooldown;
		this.shouldRender = shouldRender;
		this.icons = iconConditions;
		this.disable = disableCondition;

		if (this.icons != null) this.icons.forEach(icon -> icon.setSkill(this));
	}

	public RenderableSkill setPower(ActiveCooldownPower power) {
		this.power = power;

		return this;
	}

	public ActiveCooldownPower getPower() {
		return power;
	}

	public boolean shouldShowCooldown() {
		return this.showCooldown;
	}

	public boolean shouldRender() {
		return this.shouldRender;
	}

	public ConditionFactory<Entity>.Instance getDisableCondition() {
		return disable;
	}

	public List<RenderableIcon> getIcons() {
		return icons;
	}

	public RenderableIcon getRenderedIcon(PlayerEntity entity) {
		if (!shouldRender() || icons == null) return null;

		for (RenderableIcon icon : icons) {
			if (icon.shouldRender(entity)) return icon;
		}

		return null;
	}

	public boolean isDisabled(PlayerEntity entity) {
		return disable == null
			? false
			: disable.test(entity);
	} 
}
