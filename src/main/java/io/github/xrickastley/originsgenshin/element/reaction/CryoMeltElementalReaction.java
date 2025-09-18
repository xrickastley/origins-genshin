package io.github.xrickastley.originsgenshin.element.reaction;

import io.github.xrickastley.originsgenshin.OriginsGenshin;
import io.github.xrickastley.originsgenshin.element.Element;
import io.github.xrickastley.originsgenshin.util.TextHelper;

public final class CryoMeltElementalReaction extends AmplifyingElementalReaction {
	CryoMeltElementalReaction() {
		super(
			new ElementalReactionSettings("Melt", OriginsGenshin.identifier("melt_cryo"), TextHelper.reaction("reaction.origins-genshin.melt", "#f2be87"))
				.setReactionCoefficient(0.5)
				.setAuraElement(Element.PYRO)
				.setTriggeringElement(Element.CRYO, 2),
			1.5
		);
	}
}
