package io.github.xrickastley.originsgenshin.element.reaction;

import io.github.xrickastley.originsgenshin.OriginsGenshin;
import io.github.xrickastley.originsgenshin.element.Element;
import io.github.xrickastley.originsgenshin.util.Colors;
import io.github.xrickastley.originsgenshin.util.TextHelper;

public final class PyroSwirlElementalReaction extends AbstractSwirlElementalReaction {
	PyroSwirlElementalReaction() {
		super(
			new ElementalReactionSettings("Swirl", OriginsGenshin.identifier("swirl_pyro"), TextHelper.reaction("Swirl", Colors.ANEMO))
				.setReactionCoefficient(0.5)
				.setAuraElement(Element.PYRO, 3)
				.setTriggeringElement(Element.ANEMO, 2)
				.reversable(true)
		);
	}
}
