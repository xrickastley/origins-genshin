package io.github.xrickastley.originsgenshin.effect;

import io.github.xrickastley.originsgenshin.component.ElementComponent;
import io.github.xrickastley.originsgenshin.element.Element;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;

public final class CryoStatusEffect extends StatusEffect {
	public CryoStatusEffect() {
		super(StatusEffectCategory.HARMFUL, 0x84e8f9);

		this.addAttributeModifier(EntityAttributes.GENERIC_MOVEMENT_SPEED, "34683f1b-1465-4aba-92c3-780f4c96cac6", -0.15, EntityAttributeModifier.Operation.MULTIPLY_TOTAL);
		this.addAttributeModifier(EntityAttributes.GENERIC_ATTACK_DAMAGE, "51374b89-5cd5-4869-97e0-5da041957f52", -0.15, EntityAttributeModifier.Operation.MULTIPLY_TOTAL);
	}

	@Override
	public void applyUpdateEffect(LivingEntity entity, int amplifier) {
		final ElementComponent component = ElementComponent.KEY.get(entity);

		if (component.hasElementalApplication(Element.CRYO)) return;

		entity.removeStatusEffect(this);
	}

	@Override
	public boolean canApplyUpdateEffect(int duration, int amplifier) {
		return true;
	}
}
