package io.github.xrickastley.originsgenshin.effect;

import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.power.CooldownPower;
import io.github.xrickastley.originsgenshin.OriginsGenshin;
import io.github.xrickastley.originsgenshin.component.ElementComponent;
import io.github.xrickastley.originsgenshin.component.FrozenEffectComponent;
import io.github.xrickastley.originsgenshin.element.Element;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;

public final class FrozenStatusEffect extends StatusEffect {
	public FrozenStatusEffect() {
		super(StatusEffectCategory.HARMFUL, 0x84e8f9);

		this.addAttributeModifier(EntityAttributes.GENERIC_MOVEMENT_SPEED, "34683f1b-1465-4aba-92c3-780f4c96cac6", -1, EntityAttributeModifier.Operation.MULTIPLY_TOTAL);
		this.addAttributeModifier(EntityAttributes.GENERIC_ATTACK_DAMAGE, "51374b89-5cd5-4869-97e0-5da041957f52", Integer.MIN_VALUE, EntityAttributeModifier.Operation.ADDITION);
	}

	@Override
	public void onApplied(LivingEntity entity, int amplifier) {
		super.onApplied(entity, amplifier);

		entity
			.getWorld()
			.playSound(
				null,
				entity.getBlockPos(),
				SoundEvent.of(OriginsGenshin.identifier("frozen")),
				SoundCategory.PLAYERS,
				1.0F,
				1.0F
			);

		FrozenEffectComponent.KEY.get(entity).freeze();
	}

	@Override
	public void onRemoved(LivingEntity entity, int amplifier) {
		super.onRemoved(entity, amplifier);

		FrozenEffectComponent.KEY.get(entity).unfreeze();
	}

	@Override
	public void applyUpdateEffect(LivingEntity entity, int amplifier) {
		final ElementComponent component = ElementComponent.KEY.get(entity);

		if (!component.hasElementalApplication(Element.FREEZE)) {
			entity.removeStatusEffect(this);

			return;
		}

		PowerHolderComponent.getPowers(entity, CooldownPower.class)
			.forEach(power -> power.modify(1));

		if (entity.getStatusEffect(this).getDuration() == 1 && entity instanceof final MobEntity mob)
			mob.setAiDisabled(false);
	}

	@Override
	public boolean canApplyUpdateEffect(int duration, int amplifier) {
		return true;
	}
}
