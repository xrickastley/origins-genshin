package io.github.xrickastley.originsgenshin.element.reaction;

import io.github.xrickastley.originsgenshin.OriginsGenshin;
import io.github.xrickastley.originsgenshin.element.Element;
import io.github.xrickastley.originsgenshin.util.TextHelper;

public final class FrozenCrystallizeElementalReaction extends AbstractCrystallizeElementalReaction {
	FrozenCrystallizeElementalReaction() {
		super(
			new ElementalReactionSettings("Crystallize", OriginsGenshin.identifier("crystallize_frozen"), TextHelper.reaction("origins-genshin.element.crystallize", "#f79c00"))
				.setReactionCoefficient(0.5)
				.setAuraElement(Element.FREEZE)
				.setTriggeringElement(Element.GEO, 6)
		);
	}
}
