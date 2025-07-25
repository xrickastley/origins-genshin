package io.github.xrickastley.originsgenshin.element.reaction;

import io.github.xrickastley.originsgenshin.element.ElementalApplication;

import net.minecraft.entity.LivingEntity;

import javax.annotation.Nullable;

public abstract class AmplifyingElementalReaction extends ElementalReaction {
	final double amplifier;

	protected AmplifyingElementalReaction(ElementalReactionSettings settings, double amplifier) {
		super(settings);

		this.amplifier = amplifier;
	}

	public float applyAmplifier(float damage) {
		return (float) applyAmplifier(damage);
	}

	public double applyAmplifier(double damage) {
		return damage * amplifier;
	}

	public double getAmplifier() {
		return this.amplifier;
	}

	@Override
	protected void onReaction(LivingEntity entity, ElementalApplication auraElement, ElementalApplication triggeringElement, double reducedGauge, @Nullable LivingEntity origin) {}
}
