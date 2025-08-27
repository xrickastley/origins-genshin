package io.github.xrickastley.originsgenshin.element.reaction;

import io.github.xrickastley.originsgenshin.OriginsGenshin;
import io.github.xrickastley.originsgenshin.element.Element;
import io.github.xrickastley.originsgenshin.util.TextHelper;

public final class CryoCrystallizeElementalReaction extends AbstractCrystallizeElementalReaction {
	CryoCrystallizeElementalReaction() {
		super(
			new ElementalReactionSettings("Crystallize", OriginsGenshin.identifier("crystallize_cryo"), TextHelper.reaction("Crystallize", "#f79c00"))
				.setReactionCoefficient(0.5)
				.setAuraElement(Element.CRYO)
				.setTriggeringElement(Element.GEO, 5)
		);
	}
}
