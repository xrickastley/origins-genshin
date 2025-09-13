package io.github.xrickastley.originsgenshin.element.reaction;

import io.github.xrickastley.originsgenshin.OriginsGenshin;
import io.github.xrickastley.originsgenshin.element.Element;
import io.github.xrickastley.originsgenshin.util.Colors;
import io.github.xrickastley.originsgenshin.util.TextHelper;

public final class CryoSwirlElementalReaction extends AbstractSwirlElementalReaction {
	CryoSwirlElementalReaction() {
		super(
			new ElementalReactionSettings("Swirl", OriginsGenshin.identifier("swirl_cryo"), TextHelper.reaction("reaction.origins-genshin.swirl", Colors.ANEMO))
				.setReactionCoefficient(0.5)
				.setAuraElement(Element.CRYO, 3)
				.setTriggeringElement(Element.ANEMO, 4)
				.reversable(true)
				.preventsReactionsAfter(OriginsGenshin.identifier("swirl_frozen"))
		);
	}
}
