package io.github.xrickastley.originsgenshin.element.reaction;

import io.github.xrickastley.originsgenshin.OriginsGenshin;
import io.github.xrickastley.originsgenshin.element.Element;
import io.github.xrickastley.originsgenshin.util.TextHelper;

public final class SuperconductElementalReaction extends AbstractSuperconductElementalReaction {
	SuperconductElementalReaction() {
		super(
			new ElementalReactionSettings("Superconduct", OriginsGenshin.identifier("superconduct"), TextHelper.reaction("reaction.origins-genshin.superconduct", "#bcb0ff"))
				.setReactionCoefficient(1.0)
				.setAuraElement(Element.CRYO, 1)
				.setTriggeringElement(Element.ELECTRO, 6)
				.reversable(true)
				.preventsReactionsAfter(OriginsGenshin.identifier("superconduct_frozen"))
		);
	}
}
