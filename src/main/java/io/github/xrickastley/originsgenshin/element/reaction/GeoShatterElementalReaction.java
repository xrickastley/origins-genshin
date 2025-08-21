package io.github.xrickastley.originsgenshin.element.reaction;

import io.github.xrickastley.originsgenshin.OriginsGenshin;
import io.github.xrickastley.originsgenshin.element.Element;
import io.github.xrickastley.originsgenshin.factory.OriginsGenshinParticleFactory;

public final class GeoShatterElementalReaction extends ShatterElementalReaction {
	GeoShatterElementalReaction() {
		super(
			new ElementalReactionSettings("Shatter", OriginsGenshin.identifier("shatter_geo"), OriginsGenshinParticleFactory.SHATTER)
				.setReactionCoefficient(0)
				.setAuraElement(Element.FREEZE)
				.setTriggeringElement(Element.GEO, 1)
		);
	}
}
