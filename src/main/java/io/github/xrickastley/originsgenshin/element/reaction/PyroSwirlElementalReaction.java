package io.github.xrickastley.originsgenshin.element.reaction;

import io.github.xrickastley.originsgenshin.OriginsGenshin;
import io.github.xrickastley.originsgenshin.element.Element;
import io.github.xrickastley.originsgenshin.factory.OriginsGenshinParticleFactory;

public final class PyroSwirlElementalReaction extends AbstractSwirlElementalReaction {
	PyroSwirlElementalReaction() {
		super(
			new ElementalReactionSettings("Swirl (Pyro)", OriginsGenshin.identifier("swirl_pyro"), OriginsGenshinParticleFactory.SWIRL)
				.setReactionCoefficient(0.5)
				.setAuraElement(Element.PYRO, 3)
				.setTriggeringElement(Element.ANEMO, 2)
				.reversable(true)
		);
	}
}
