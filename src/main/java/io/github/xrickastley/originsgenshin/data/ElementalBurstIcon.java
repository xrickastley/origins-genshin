package io.github.xrickastley.originsgenshin.data;

import io.github.apace100.apoli.power.PowerType;
import io.github.apace100.apoli.power.factory.condition.ConditionFactory;
import io.github.xrickastley.originsgenshin.util.Color;

import net.minecraft.entity.Entity;
import net.minecraft.util.Identifier;

public class ElementalBurstIcon extends RenderableIcon {
	protected final PowerType<?> resource;
	protected final Color color;
	protected final Color outlineColor;
	protected final int newMax;

	public ElementalBurstIcon(Identifier icon, PowerType<?> cooldown, boolean reverse, PowerType<?> resource, Color color, Color outlineColor, int newMax, ConditionFactory<Entity>.Instance condition, ConditionFactory<Entity>.Instance disableCondition) {
		super(icon, cooldown, reverse, condition, disableCondition);

		this.resource = resource;
		this.color = color;
		this.outlineColor = outlineColor;
		this.newMax = newMax;
	}

	public PowerType<?> getResource() {
		return resource;
	}

	public Color getColor() {
		return color;
	}

	public Color getOutlineColor() {
		return outlineColor;
	}

	public int getNewMax() {
		return newMax;
	}

	@Override
	public ElementalBurst getSkill() {
		return ((ElementalBurst) super.getSkill());
	}
}
