package io.github.xrickastley.originsgenshin.elements.reactions;

import io.github.xrickastley.originsgenshin.OriginsGenshin;
import io.github.xrickastley.originsgenshin.elements.Element;
import io.github.xrickastley.originsgenshin.factories.OriginsGenshinParticleFactory;

public final class HydroVaporizeElementalReaction extends AmplifyingElementalReaction {
	protected HydroVaporizeElementalReaction() {
		super(
			new ElementalReactionSettings("vaporize", OriginsGenshin.identifier("vaporize_hydro"), OriginsGenshinParticleFactory.Vaporize)
				.setReactionCoefficient(2.0)
				.setAuraElement(Element.PYRO, 1)
				.setTriggeringElement(Element.HYDRO),
			2
		);
	}
}
