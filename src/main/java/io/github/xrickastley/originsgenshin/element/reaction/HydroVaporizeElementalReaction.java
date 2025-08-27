package io.github.xrickastley.originsgenshin.element.reaction;

import io.github.xrickastley.originsgenshin.OriginsGenshin;
import io.github.xrickastley.originsgenshin.element.Element;
import io.github.xrickastley.originsgenshin.util.TextHelper;

public final class HydroVaporizeElementalReaction extends AmplifyingElementalReaction {
	HydroVaporizeElementalReaction() {
		super(
			new ElementalReactionSettings("Vaporize", OriginsGenshin.identifier("vaporize_hydro"), TextHelper.reaction("Vaporize", "#f2be87"))
				.setReactionCoefficient(2.0)
				.setAuraElement(Element.PYRO)
				.setTriggeringElement(Element.HYDRO, 1),
			2
		);
	}
}
