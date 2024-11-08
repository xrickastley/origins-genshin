package io.github.xrickastley.originsgenshin.elements.reactions;

import io.github.xrickastley.originsgenshin.OriginsGenshin;
import io.github.xrickastley.originsgenshin.elements.Element;
import io.github.xrickastley.originsgenshin.factories.OriginsGenshinParticleFactory;

public final class PyroMeltElementalReaction extends AmplifyingElementalReaction {
	protected PyroMeltElementalReaction() {
		super(
			new ElementalReactionSettings("Melt", OriginsGenshin.identifier("melt_pyro"), OriginsGenshinParticleFactory.Vaporize)
				.setReactionCoefficient(2.0)
				.setAuraElement(Element.CRYO, 5)
				.setTriggeringElement(Element.PYRO),
			2
		);
	}
}
