package io.github.xrickastley.originsgenshin.element.reaction;

import io.github.xrickastley.originsgenshin.OriginsGenshin;
import io.github.xrickastley.originsgenshin.element.Element;
import io.github.xrickastley.originsgenshin.factory.OriginsGenshinParticleFactory;

public class CryoSwirlElementalReaction extends AbstractSwirlElementalReaction {
	public CryoSwirlElementalReaction() {
		super(
			new ElementalReactionSettings("Swirl", OriginsGenshin.identifier("swirl_cryo"), OriginsGenshinParticleFactory.Swirl)
				.setReactionCoefficient(0.5)
				.setAuraElement(Element.CRYO, 4)
				.setTriggeringElement(Element.ANEMO, 3)
				.setAsReversable(true)
		);
	}
}
