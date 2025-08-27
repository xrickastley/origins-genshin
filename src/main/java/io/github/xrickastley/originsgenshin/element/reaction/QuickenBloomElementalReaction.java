package io.github.xrickastley.originsgenshin.element.reaction;

import io.github.xrickastley.originsgenshin.OriginsGenshin;
import io.github.xrickastley.originsgenshin.element.Element;
import io.github.xrickastley.originsgenshin.util.TextHelper;

public final class QuickenBloomElementalReaction extends AbstractBloomElementalReaction {
	QuickenBloomElementalReaction() {
		super(
			new ElementalReactionSettings("Bloom", OriginsGenshin.identifier("bloom_quicken"), TextHelper.reaction("Bloom", "#01e858"))
				.setReactionCoefficient(2.0)
				.setAuraElement(Element.QUICKEN)
				.setTriggeringElement(Element.HYDRO, 4)
		);
	}
}
