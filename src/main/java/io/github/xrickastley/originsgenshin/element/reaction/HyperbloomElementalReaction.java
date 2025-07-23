package io.github.xrickastley.originsgenshin.element.reaction;

import javax.annotation.Nullable;

import io.github.xrickastley.originsgenshin.OriginsGenshin;
import io.github.xrickastley.originsgenshin.entity.DendroCoreEntity;
import io.github.xrickastley.originsgenshin.factory.OriginsGenshinParticleFactory;
import net.minecraft.entity.LivingEntity;

public final class HyperbloomElementalReaction extends AbstractDendroCoreElementalReaction {
	HyperbloomElementalReaction() {
		super(
			new ElementalReactionSettings("Hyperbloom", OriginsGenshin.identifier("hyperbloom"), OriginsGenshinParticleFactory.HYPERBLOOM)
		);
	}

	@Override
	protected void onReaction(DendroCoreEntity dendroCore, @Nullable LivingEntity origin) {
		dendroCore.addOwner(origin).setAsHyperbloom();
	}
}
