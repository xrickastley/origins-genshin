package io.github.xrickastley.originsgenshin.element.reaction;

import io.github.xrickastley.originsgenshin.OriginsGenshin;
import io.github.xrickastley.originsgenshin.element.Element;
import io.github.xrickastley.originsgenshin.util.Colors;
import io.github.xrickastley.originsgenshin.util.TextHelper;

public final class ElectroSwirlElementalReaction extends AbstractSwirlElementalReaction {
	ElectroSwirlElementalReaction() {
		super(
			new ElementalReactionSettings("Swirl", OriginsGenshin.identifier("swirl_electro"), TextHelper.reaction("Swirl", Colors.ANEMO))
				.setReactionCoefficient(0.5)
				.setAuraElement(Element.ELECTRO, 4)
				.setTriggeringElement(Element.ANEMO, 1)
				.reversable(true)
		);
	}
}
