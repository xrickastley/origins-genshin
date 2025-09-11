package io.github.xrickastley.originsgenshin.element.reaction;

import io.github.xrickastley.originsgenshin.OriginsGenshin;
import io.github.xrickastley.originsgenshin.element.Element;
import io.github.xrickastley.originsgenshin.util.Colors;
import io.github.xrickastley.originsgenshin.util.TextHelper;

public final class AggravateElementalReaction extends AdditiveElementalReaction {
	AggravateElementalReaction() {
		super(
			new ElementalReactionSettings("Aggravate", OriginsGenshin.identifier("aggravate"), TextHelper.reaction("origins-genshin.element.aggravate", Colors.ELECTRO))
				.setReactionCoefficient(0)
				.setAuraElement(Element.QUICKEN)
				.setTriggeringElement(Element.ELECTRO, 2)
				.applyResultAsAura(true),
			1.15
		);
	}
}
