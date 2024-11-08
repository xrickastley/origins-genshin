package io.github.xrickastley.originsgenshin.elements.reactions;

import io.github.xrickastley.originsgenshin.OriginsGenshin;
import io.github.xrickastley.originsgenshin.elements.Element;
import io.github.xrickastley.originsgenshin.factories.OriginsGenshinParticleFactory;

public final class PyroVaporizeElementalReaction extends AmplifyingElementalReaction {
	protected PyroVaporizeElementalReaction() {
		super(
			new ElementalReactionSettings("vaporize", OriginsGenshin.identifier("vaporize_pyro"), OriginsGenshinParticleFactory.Vaporize)
				.setReactionCoefficient(0.5)
				.setAuraElement(Element.HYDRO, 1)
				.setTriggeringElement(Element.PYRO),
			1.5
		);
	}
}
