package io.github.xrickastley.originsgenshin.element.reaction;

import io.github.xrickastley.originsgenshin.OriginsGenshin;
import io.github.xrickastley.originsgenshin.element.Element;
import io.github.xrickastley.originsgenshin.util.TextHelper;

public final class GeoShatterElementalReaction extends ShatterElementalReaction {
	GeoShatterElementalReaction() {
		super(
			new ElementalReactionSettings("Shatter", OriginsGenshin.identifier("shatter_geo"), TextHelper.reaction("reaction.origins-genshin.shatter", "#cfffff"))
				.setReactionCoefficient(0)
				.setAuraElement(Element.FREEZE)
				.setTriggeringElement(Element.GEO, 1)
		);
	}
}
