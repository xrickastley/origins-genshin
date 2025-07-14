package io.github.xrickastley.originsgenshin.element.reaction;

import io.github.xrickastley.originsgenshin.OriginsGenshin;
import io.github.xrickastley.originsgenshin.element.Element;
import io.github.xrickastley.originsgenshin.factory.OriginsGenshinParticleFactory;

public class SpreadElementalReaction extends AdditiveElementalReaction {
	public SpreadElementalReaction() {
		super(
			new ElementalReactionSettings("Spread", OriginsGenshin.identifier("spread"), OriginsGenshinParticleFactory.SPREAD)
				// Triggering Frozen should consume the entirety of both Cryo and Hydro aura.
				.setReactionCoefficient(0)
				.setAuraElement(Element.QUICKEN)
				.setTriggeringElement(Element.DENDRO, 1),
			1.25
		);
	}
}
