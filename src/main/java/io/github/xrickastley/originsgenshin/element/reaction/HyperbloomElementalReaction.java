package io.github.xrickastley.originsgenshin.element.reaction;

import io.github.xrickastley.originsgenshin.OriginsGenshin;
import io.github.xrickastley.originsgenshin.entity.DendroCoreEntity;
import io.github.xrickastley.originsgenshin.util.Colors;
import io.github.xrickastley.originsgenshin.util.TextHelper;

import net.minecraft.entity.LivingEntity;

import javax.annotation.Nullable;

public final class HyperbloomElementalReaction extends AbstractDendroCoreElementalReaction {
	HyperbloomElementalReaction() {
		super(
			new ElementalReactionSettings("Hyperbloom", OriginsGenshin.identifier("hyperbloom"), TextHelper.reaction("origins-genshin.element.hyperbloom", Colors.ELECTRO))
		);
	}

	@Override
	protected void onReaction(DendroCoreEntity dendroCore, @Nullable LivingEntity origin) {
		dendroCore.addOwner(origin).setAsHyperbloom();
	}
}
