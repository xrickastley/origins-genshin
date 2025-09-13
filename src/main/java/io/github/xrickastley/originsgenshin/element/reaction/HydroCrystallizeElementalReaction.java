package io.github.xrickastley.originsgenshin.element.reaction;

import io.github.xrickastley.originsgenshin.OriginsGenshin;
import io.github.xrickastley.originsgenshin.element.Element;
import io.github.xrickastley.originsgenshin.util.TextHelper;

public final class HydroCrystallizeElementalReaction extends AbstractCrystallizeElementalReaction {
	HydroCrystallizeElementalReaction() {
		super(
			new ElementalReactionSettings("Crystallize", OriginsGenshin.identifier("crystallize_hydro"), TextHelper.reaction("reaction.origins-genshin.crystallize", "#f79c00"))
				.setReactionCoefficient(0.5)
				.setAuraElement(Element.HYDRO)
				.setTriggeringElement(Element.GEO, 4)
		);
	}
}
