package io.github.xrickastley.originsgenshin.data;

import java.util.List;

import io.github.apace100.apoli.power.factory.condition.ConditionFactory;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;

public class ElementalBurst extends RenderableSkill {
	public ElementalBurst(boolean showCooldown, boolean shouldRender, ConditionFactory<Entity>.Instance disableCondition, List<RenderableIcon> iconConditions) {
		super(showCooldown, shouldRender, disableCondition, iconConditions);
	}

	@Override
	public ElementalBurstIcon getRenderedIcon(PlayerEntity entity) {
		return ((ElementalBurstIcon) super.getRenderedIcon(entity));
	}
}
