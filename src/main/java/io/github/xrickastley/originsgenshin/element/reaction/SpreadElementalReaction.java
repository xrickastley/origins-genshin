package io.github.xrickastley.originsgenshin.element.reaction;

import io.github.xrickastley.originsgenshin.OriginsGenshin;
import io.github.xrickastley.originsgenshin.element.Element;
import io.github.xrickastley.originsgenshin.factory.OriginsGenshinParticleFactory;

public final class SpreadElementalReaction extends AdditiveElementalReaction {
	SpreadElementalReaction() {
		super(
			new ElementalReactionSettings("Spread", OriginsGenshin.identifier("spread"), OriginsGenshinParticleFactory.SPREAD)
				.setReactionCoefficient(0)
				.setAuraElement(Element.QUICKEN)
				.setTriggeringElement(Element.DENDRO, 1)
				.applyResultAsAura(true),
			1.25
		);
	}
}
