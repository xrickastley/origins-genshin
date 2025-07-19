package io.github.xrickastley.originsgenshin.element.reaction;

import javax.annotation.Nullable;

import io.github.xrickastley.originsgenshin.element.ElementalApplication;
import io.github.xrickastley.originsgenshin.entity.DendroCoreEntity;
import io.github.xrickastley.originsgenshin.factory.OriginsGenshinEntities;

import net.minecraft.entity.LivingEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;

public abstract sealed class AbstractBloomElementalReaction 
	extends ElementalReaction 
	permits DendroBloomElementalReaction, HydroBloomElementalReaction, QuickenBloomElementalReaction
{
	AbstractBloomElementalReaction(ElementalReactionSettings settings) {
		super(settings);
	}

	@Override
	protected void onReaction(LivingEntity entity, ElementalApplication auraElement, ElementalApplication triggeringElement, double reducedGauge, @Nullable LivingEntity origin) {
		final World world = entity.getWorld();

		if (!(world instanceof final ServerWorld serverWorld)) return;

		final DendroCoreEntity dendroCore = new DendroCoreEntity(OriginsGenshinEntities.DENDRO_CORE, serverWorld, origin);
		dendroCore.setPosition(entity.getPos());

		serverWorld.spawnNewEntityAndPassengers(dendroCore);
	}
}
