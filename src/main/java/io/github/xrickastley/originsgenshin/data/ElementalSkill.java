package io.github.xrickastley.originsgenshin.data;

import java.util.List;

import io.github.apace100.apoli.power.factory.condition.ConditionFactory;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;

public class ElementalSkill extends RenderableSkill {
	public ElementalSkill(boolean showCooldown, boolean shouldRender, ConditionFactory<Entity>.Instance disableCondition, List<RenderableIcon> iconConditions) {
		super(showCooldown, shouldRender, disableCondition, iconConditions);
	}

	@Override
	public ElementalSkillIcon getRenderedIcon(PlayerEntity entity) {
		return ((ElementalSkillIcon) super.getRenderedIcon(entity));
	}
}