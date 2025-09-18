package io.github.xrickastley.originsgenshin.element.reaction;

import io.github.xrickastley.originsgenshin.element.ElementalApplication;
import io.github.xrickastley.originsgenshin.entity.DendroCoreEntity;
import io.github.xrickastley.originsgenshin.events.ReactionTriggered;

import net.minecraft.entity.LivingEntity;

import javax.annotation.Nullable;

public abstract sealed class AbstractDendroCoreElementalReaction
	extends ElementalReaction
	permits HyperbloomElementalReaction, BurgeonElementalReaction
{
	AbstractDendroCoreElementalReaction(ElementalReactionSettings settings) {
		super(settings);
	}

	@Override
	public boolean isTriggerable(LivingEntity entity) {
		return entity instanceof DendroCoreEntity;
	}

	@Override
	public boolean trigger(LivingEntity entity, @Nullable LivingEntity origin) {
		if (!(entity instanceof final DendroCoreEntity dendroCore)) throw new ClassCastException("This reaction may only be triggered on a Dendro Core! Provided entity: " + entity);

		if (!dendroCore.isNormal()) return false;

		this.onReaction(dendroCore, origin);
		this.displayReaction(entity);

		ReactionTriggered.EVENT
			.invoker()
			.onReactionTriggered(this, 0, entity, origin);

		return true;
	}

	@Override
	protected void onReaction(LivingEntity entity, @Nullable ElementalApplication auraElement, ElementalApplication triggeringElement, double reducedGauge, @Nullable LivingEntity origin) {}

	protected abstract void onReaction(DendroCoreEntity dendroCore, @Nullable LivingEntity origin);
}
