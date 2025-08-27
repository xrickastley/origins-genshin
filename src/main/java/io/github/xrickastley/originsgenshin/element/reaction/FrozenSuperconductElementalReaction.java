package io.github.xrickastley.originsgenshin.element.reaction;

import io.github.xrickastley.originsgenshin.OriginsGenshin;
import io.github.xrickastley.originsgenshin.element.Element;
import io.github.xrickastley.originsgenshin.util.TextHelper;

public final class FrozenSuperconductElementalReaction extends AbstractSuperconductElementalReaction {
	FrozenSuperconductElementalReaction() {
		super(
			new ElementalReactionSettings("Superconduct", OriginsGenshin.identifier("superconduct_frozen"), TextHelper.reaction("Superconduct", "#bcb0ff"))
				.setReactionCoefficient(1.0)
				.setAuraElement(Element.FREEZE, 2)
				.setTriggeringElement(Element.ELECTRO, 7)
				.reversable(true)
				.preventsReactionsAfter()
		);
	}
}
