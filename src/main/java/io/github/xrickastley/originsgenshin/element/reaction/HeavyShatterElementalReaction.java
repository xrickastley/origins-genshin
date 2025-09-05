package io.github.xrickastley.originsgenshin.element.reaction;

import io.github.xrickastley.originsgenshin.OriginsGenshin;
import io.github.xrickastley.originsgenshin.component.ElementComponent;
import io.github.xrickastley.originsgenshin.element.Element;
import io.github.xrickastley.originsgenshin.util.TextHelper;

import net.minecraft.entity.LivingEntity;
import net.minecraft.item.AxeItem;
import net.minecraft.item.PickaxeItem;
import net.minecraft.item.ToolItem;

public final class HeavyShatterElementalReaction extends ShatterElementalReaction {
	HeavyShatterElementalReaction() {
		super(
			new ElementalReactionSettings("Shatter", OriginsGenshin.identifier("shatter_heavy"), TextHelper.font(TextHelper.gradient("Shatter", 0xcfffff, 0x70dee4), TextHelper.GENSHIN_FONT))
				.setReactionCoefficient(0)
				.setAuraElement(Element.FREEZE)
				.setTriggeringElement(Element.PHYSICAL, 0)
		);
	}

	@Override
	public boolean isTriggerable(LivingEntity entity) {
		return ElementComponent.KEY.get(entity).hasElementalApplication(Element.FREEZE)
			&& entity.originsgenshin$getPlannedAttacker() != null
			&& entity.originsgenshin$getPlannedAttacker() instanceof final LivingEntity attacker
			&& attacker.getMainHandStack().getItem() instanceof final ToolItem tool
			&& (tool instanceof AxeItem || tool instanceof PickaxeItem);
	}
}
