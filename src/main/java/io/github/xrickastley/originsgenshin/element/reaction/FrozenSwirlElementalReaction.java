package io.github.xrickastley.originsgenshin.element.reaction;

import io.github.xrickastley.originsgenshin.OriginsGenshin;
import io.github.xrickastley.originsgenshin.element.Element;
import io.github.xrickastley.originsgenshin.util.Colors;
import io.github.xrickastley.originsgenshin.util.TextHelper;

public final class FrozenSwirlElementalReaction extends AbstractSwirlElementalReaction {
	FrozenSwirlElementalReaction() {
		super(
			new ElementalReactionSettings("Swirl", OriginsGenshin.identifier("swirl_frozen"), TextHelper.reaction("Swirl", Colors.ANEMO))
				.setReactionCoefficient(0.5)
				.setAuraElement(Element.FREEZE, 4)
				.setTriggeringElement(Element.ANEMO, 5)
				.reversable(true),
			Element.CRYO
		);
	}
}
