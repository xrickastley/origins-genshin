package io.github.xrickastley.originsgenshin.element.reaction;

import io.github.xrickastley.originsgenshin.OriginsGenshin;
import io.github.xrickastley.originsgenshin.element.Element;
import io.github.xrickastley.originsgenshin.factory.OriginsGenshinParticleFactory;

public final class HydroVaporizeElementalReaction extends AmplifyingElementalReaction {
	protected HydroVaporizeElementalReaction() {
		super(
			new ElementalReactionSettings("Vaporize", OriginsGenshin.identifier("vaporize_hydro"), OriginsGenshinParticleFactory.VAPORIZE)
				.setReactionCoefficient(2.0)
				.setAuraElement(Element.PYRO, 1)
				.setTriggeringElement(Element.HYDRO),
			2
		);
	}
}
