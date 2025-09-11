package io.github.xrickastley.originsgenshin.element.reaction;

import io.github.xrickastley.originsgenshin.OriginsGenshin;
import io.github.xrickastley.originsgenshin.element.Element;
import io.github.xrickastley.originsgenshin.util.Colors;
import io.github.xrickastley.originsgenshin.util.TextHelper;

public final class BurningElementalReaction extends AbstractBurningElementalReaction {
	BurningElementalReaction() {
		super(
			new ElementalReactionSettings("Burning", OriginsGenshin.identifier("burning"), TextHelper.reaction("origins-genshin.element.burning", Colors.PYRO))
				.setReactionCoefficient(0) // Coefficient: 0 since Burning is "special", removes itself when Dendro is gone/by natural causes.
				.setAuraElement(Element.DENDRO, 3)
				.setTriggeringElement(Element.PYRO, 6)
				.applyResultAsAura(true)
				.reversable(true)
		);
	}
}
