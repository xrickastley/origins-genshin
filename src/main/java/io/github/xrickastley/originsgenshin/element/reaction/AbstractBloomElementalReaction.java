package io.github.xrickastley.originsgenshin.element.reaction;

import io.github.xrickastley.originsgenshin.element.ElementalApplication;
import io.github.xrickastley.originsgenshin.entity.DendroCoreEntity;
import io.github.xrickastley.originsgenshin.factory.OriginsGenshinEntities;

import net.minecraft.entity.LivingEntity;
import net.minecraft.world.World;

public abstract class AbstractBloomElementalReaction extends ElementalReaction {
	protected AbstractBloomElementalReaction(ElementalReactionSettings settings) {
		super(settings);
	}

	@Override
	protected void onReaction(LivingEntity entity, ElementalApplication auraElement, ElementalApplication triggeringElement, double reducedGauge) {
		final World world = entity.getWorld();

		if (world.isClient) return;

		world.spawnEntity(
			new DendroCoreEntity(OriginsGenshinEntities.DENDRO_CORE, world)
		);
	}
}
