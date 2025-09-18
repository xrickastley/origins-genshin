package io.github.xrickastley.originsgenshin.element.reaction;

import io.github.xrickastley.originsgenshin.OriginsGenshin;
import io.github.xrickastley.originsgenshin.element.Element;
import io.github.xrickastley.originsgenshin.util.TextHelper;

public final class HydroBloomElementalReaction extends AbstractBloomElementalReaction {
	HydroBloomElementalReaction() {
		super(
			new ElementalReactionSettings("Bloom", OriginsGenshin.identifier("bloom_hydro"), TextHelper.reaction("reaction.origins-genshin.bloom", "#01e858"))
				.setReactionCoefficient(0.5)
				.setAuraElement(Element.DENDRO)
				.setTriggeringElement(Element.HYDRO, 4)
				.preventsReactionsAfter(OriginsGenshin.identifier("spread"), OriginsGenshin.identifier("bloom_quicken"))
		);
	}
}
