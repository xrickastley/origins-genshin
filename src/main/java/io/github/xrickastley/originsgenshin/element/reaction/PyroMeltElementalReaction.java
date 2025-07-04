package io.github.xrickastley.originsgenshin.element.reaction;

import io.github.xrickastley.originsgenshin.OriginsGenshin;
import io.github.xrickastley.originsgenshin.element.Element;
import io.github.xrickastley.originsgenshin.factory.OriginsGenshinParticleFactory;

public final class PyroMeltElementalReaction extends AmplifyingElementalReaction {
	protected PyroMeltElementalReaction() {
		super(
			new ElementalReactionSettings("Melt (Pyro)", OriginsGenshin.identifier("melt_pyro"), OriginsGenshinParticleFactory.MELT)
				.setReactionCoefficient(2.0)
				.setAuraElement(Element.CRYO, 5)
				.setTriggeringElement(Element.PYRO),
			2
		);
	}
}
