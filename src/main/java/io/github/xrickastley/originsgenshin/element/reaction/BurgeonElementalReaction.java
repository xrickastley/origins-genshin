package io.github.xrickastley.originsgenshin.element.reaction;

import io.github.xrickastley.originsgenshin.OriginsGenshin;
import io.github.xrickastley.originsgenshin.entity.DendroCoreEntity;
import io.github.xrickastley.originsgenshin.factory.OriginsGenshinParticleFactory;

import net.minecraft.entity.LivingEntity;

import javax.annotation.Nullable;

public final class BurgeonElementalReaction extends AbstractDendroCoreElementalReaction {
	BurgeonElementalReaction() {
		super(
			new ElementalReactionSettings("Burgeon", OriginsGenshin.identifier("burgeon"), OriginsGenshinParticleFactory.BURGEON)
		);
	}

	@Override
	protected void onReaction(DendroCoreEntity dendroCore, @Nullable LivingEntity origin) {
		dendroCore.addOwner(origin).setAsBurgeon();
	}
}
