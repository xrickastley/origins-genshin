package io.github.xrickastley.originsgenshin.element.reaction;

import io.github.xrickastley.originsgenshin.OriginsGenshin;
import io.github.xrickastley.originsgenshin.element.Element;
import io.github.xrickastley.originsgenshin.util.TextHelper;

public final class PyroVaporizeElementalReaction extends AmplifyingElementalReaction {
	PyroVaporizeElementalReaction() {
		super(
			new ElementalReactionSettings("Vaporize", OriginsGenshin.identifier("vaporize_pyro"), TextHelper.reaction("origins-genshin.element.vaporize", "#f2be87"))
				.setReactionCoefficient(0.5)
				.setAuraElement(Element.HYDRO)
				.setTriggeringElement(Element.PYRO, 5),
			1.5
		);
	}
}
