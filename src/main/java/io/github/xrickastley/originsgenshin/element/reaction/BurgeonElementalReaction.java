package io.github.xrickastley.originsgenshin.element.reaction;

import javax.annotation.Nullable;

import io.github.xrickastley.originsgenshin.OriginsGenshin;
import io.github.xrickastley.originsgenshin.entity.DendroCoreEntity;
import io.github.xrickastley.originsgenshin.factory.OriginsGenshinParticleFactory;
import net.minecraft.entity.LivingEntity;

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
