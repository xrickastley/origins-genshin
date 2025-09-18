package io.github.xrickastley.originsgenshin.element.reaction;

import io.github.xrickastley.originsgenshin.OriginsGenshin;
import io.github.xrickastley.originsgenshin.element.Element;
import io.github.xrickastley.originsgenshin.util.Colors;
import io.github.xrickastley.originsgenshin.util.TextHelper;

public final class HydroSwirlElementalReaction extends AbstractSwirlElementalReaction {
	HydroSwirlElementalReaction() {
		super(
			new ElementalReactionSettings("Swirl", OriginsGenshin.identifier("swirl_hydro"), TextHelper.reaction("reaction.origins-genshin.swirl", Colors.ANEMO))
				.setReactionCoefficient(0.5)
				.setAuraElement(Element.HYDRO, 2)
				.setTriggeringElement(Element.ANEMO, 3)
				.reversable(true),
			true
		);
	}
}
