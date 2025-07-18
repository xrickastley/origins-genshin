package io.github.xrickastley.originsgenshin.element.reaction;

import io.github.xrickastley.originsgenshin.OriginsGenshin;
import io.github.xrickastley.originsgenshin.element.Element;
import io.github.xrickastley.originsgenshin.factory.OriginsGenshinParticleFactory;

public final class CryoSwirlElementalReaction extends AbstractSwirlElementalReaction {
	public CryoSwirlElementalReaction() {
		super(
			new ElementalReactionSettings("Swirl", OriginsGenshin.identifier("swirl_cryo"), OriginsGenshinParticleFactory.SWIRL)
				.setReactionCoefficient(0.5)
				.setAuraElement(Element.CRYO, 3)
				.setTriggeringElement(Element.ANEMO, 4)
				.reversable(true)
		);
	}
}
