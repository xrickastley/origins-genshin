package io.github.xrickastley.originsgenshin.element.reaction;

import io.github.xrickastley.originsgenshin.OriginsGenshin;
import io.github.xrickastley.originsgenshin.element.Element;
import io.github.xrickastley.originsgenshin.factory.OriginsGenshinParticleFactory;

public final class FrozenSuperconductElementalReaction extends AbstractSuperconductElementalReaction {
	FrozenSuperconductElementalReaction() {
		super(
			new ElementalReactionSettings("Superconduct (Frozen)", OriginsGenshin.identifier("superconduct_frozen"), OriginsGenshinParticleFactory.SUPERCONDUCT)
				.setReactionCoefficient(1.0)
				.setAuraElement(Element.FROZEN, 2)
				.setTriggeringElement(Element.ELECTRO, 7)
				.reversable(true)
				.preventsReactionsAfter()
		);
	}
}
