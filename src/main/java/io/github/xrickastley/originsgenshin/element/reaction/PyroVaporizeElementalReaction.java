package io.github.xrickastley.originsgenshin.element.reaction;

import io.github.xrickastley.originsgenshin.OriginsGenshin;
import io.github.xrickastley.originsgenshin.element.Element;
import io.github.xrickastley.originsgenshin.factory.OriginsGenshinParticleFactory;

public final class PyroVaporizeElementalReaction extends AmplifyingElementalReaction {
	PyroVaporizeElementalReaction() {
		super(
			new ElementalReactionSettings("Vaporize", OriginsGenshin.identifier("vaporize_pyro"), OriginsGenshinParticleFactory.VAPORIZE)
				.setReactionCoefficient(0.5)
				.setAuraElement(Element.HYDRO)
				.setTriggeringElement(Element.PYRO, 5),
			1.5
		);
	}
}
