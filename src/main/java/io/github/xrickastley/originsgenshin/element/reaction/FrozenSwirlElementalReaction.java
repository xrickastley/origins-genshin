package io.github.xrickastley.originsgenshin.element.reaction;

import io.github.xrickastley.originsgenshin.OriginsGenshin;
import io.github.xrickastley.originsgenshin.element.Element;
import io.github.xrickastley.originsgenshin.factory.OriginsGenshinParticleFactory;

public final class FrozenSwirlElementalReaction extends AbstractSwirlElementalReaction {
	public FrozenSwirlElementalReaction() {
		super(
			new ElementalReactionSettings("Swirl", OriginsGenshin.identifier("swirl_frozen"), OriginsGenshinParticleFactory.SWIRL)
				.setReactionCoefficient(0.5)
				.setAuraElement(Element.FROZEN, 4)
				.setTriggeringElement(Element.ANEMO, 5)
				.reversable(true),
			Element.CRYO
		);
	}
}
