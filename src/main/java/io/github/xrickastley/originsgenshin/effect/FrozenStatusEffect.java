package io.github.xrickastley.originsgenshin.effect;

import com.google.common.collect.HashMultimap;

import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.power.CooldownPower;
import io.github.xrickastley.originsgenshin.OriginsGenshin;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.AttributeContainer;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;

public class FrozenStatusEffect extends StatusEffect {
	public FrozenStatusEffect() {
		super(StatusEffectCategory.HARMFUL, 0x84e8f9);

		attributeMultimap.put(EntityAttributes.GENERIC_MOVEMENT_SPEED, slowEffect);
		attributeMultimap.put(EntityAttributes.GENERIC_ATTACK_DAMAGE, weakEffect);
	}
	
	private final HashMultimap<EntityAttribute, EntityAttributeModifier> attributeMultimap = HashMultimap.create();
	private EntityAttributeModifier slowEffect = new EntityAttributeModifier("Frozen: Slowness", -1, EntityAttributeModifier.Operation.MULTIPLY_TOTAL);
	private EntityAttributeModifier weakEffect = new EntityAttributeModifier("Frozen: Weakness", -1, EntityAttributeModifier.Operation.MULTIPLY_TOTAL);

	@Override
	public void onApplied(LivingEntity entity, int amplifier) {
		final AttributeContainer attributes = entity.getAttributes();

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

		attributes.addTemporaryModifiers(attributeMultimap);
	}

	@Override
	public void onRemoved(AttributeContainer attributes) {
		attributes.removeModifiers(attributeMultimap);
	}

	@Override
	public void applyUpdateEffect(LivingEntity entity, int amplifier) {
		PowerHolderComponent.getPowers(entity, CooldownPower.class)
			.forEach(power -> power.modify(1));

		// if (entity.getStatusEffect(this).getDuration() == 1) entity.getWorld().playSound(null, entity.getBlockPos(), SoundEvent.of(Aery.identifier("frozen")), SoundCategory.PLAYERS, 1.0F, 1.0F);
	}

	@Override
	public boolean canApplyUpdateEffect(int duration, int amplifier) {
		return false;
	}
}

