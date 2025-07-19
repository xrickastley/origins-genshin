package io.github.xrickastley.originsgenshin.element.reaction;

import io.github.xrickastley.originsgenshin.OriginsGenshin;
import io.github.xrickastley.originsgenshin.element.Element;
import io.github.xrickastley.originsgenshin.factory.OriginsGenshinParticleFactory;

public final class AggravateElementalReaction extends AdditiveElementalReaction {
	AggravateElementalReaction() {
		super(
			new ElementalReactionSettings("Aggravate", OriginsGenshin.identifier("aggravate"), OriginsGenshinParticleFactory.AGGRAVATE)
				// Triggering Frozen should consume the entirety of both Cryo and Hydro aura.
				.setReactionCoefficient(0)
				.setAuraElement(Element.QUICKEN)
				.setTriggeringElement(Element.ELECTRO, 2),
			1.15
		);
	}
}
