package io.github.xrickastley.originsgenshin.elements.reactions;

import io.github.xrickastley.originsgenshin.elements.ElementalApplication;
import net.minecraft.entity.LivingEntity;

public abstract class AmplifyingElementalReaction extends ElementalReaction {
	final double amplifier;

	public AmplifyingElementalReaction(ElementalReactionSettings settings, double amplifier) {
		super(settings);

		this.amplifier = amplifier;
	}

	public float applyAmplifier(float damage) {
		return (float) applyAmplifier((double) damage);
	}

	public double applyAmplifier(double damage) {
		return damage * amplifier;
	}
	
	public double getAmplifier() {
		return this.amplifier;
	}

	@Override
	protected void onReaction(LivingEntity entity, ElementalApplication auraElement, ElementalApplication triggeringElement, double reducedGauge) {}
}
