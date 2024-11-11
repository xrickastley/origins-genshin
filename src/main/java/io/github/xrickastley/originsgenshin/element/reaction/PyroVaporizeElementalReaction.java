package io.github.xrickastley.originsgenshin.element.reaction;

import io.github.xrickastley.originsgenshin.OriginsGenshin;
import io.github.xrickastley.originsgenshin.element.Element;
import io.github.xrickastley.originsgenshin.factory.OriginsGenshinParticleFactory;

public final class PyroVaporizeElementalReaction extends AmplifyingElementalReaction {
	protected PyroVaporizeElementalReaction() {
		super(
			new ElementalReactionSettings("Vaporize", OriginsGenshin.identifier("vaporize_pyro"), OriginsGenshinParticleFactory.Vaporize)
				.setReactionCoefficient(0.5)
				.setAuraElement(Element.HYDRO, 1)
				.setTriggeringElement(Element.PYRO),
			1.5
		);
	}
}
