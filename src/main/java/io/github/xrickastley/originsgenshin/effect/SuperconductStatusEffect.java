package io.github.xrickastley.originsgenshin.effect;

import io.github.xrickastley.originsgenshin.factory.OriginsGenshinAttributes;

import net.minecraft.entity.attribute.EntityAttributeModifier.Operation;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;

public final class SuperconductStatusEffect extends StatusEffect {
	public SuperconductStatusEffect() {
		super(StatusEffectCategory.HARMFUL, 0xbcb0ff);

		this.addAttributeModifier(OriginsGenshinAttributes.PHYSICAL_RES, "286e4184-4441-4be1-92ba-26464b79a8bc", -40, Operation.ADDITION);
	}
}
