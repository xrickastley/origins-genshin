package io.github.xrickastley.originsgenshin.element.reaction;

import io.github.xrickastley.originsgenshin.OriginsGenshin;
import io.github.xrickastley.originsgenshin.element.Element;
import io.github.xrickastley.originsgenshin.util.Colors;
import io.github.xrickastley.originsgenshin.util.TextHelper;

public final class QuickenBurningElementalReaction extends AbstractBurningElementalReaction {
	QuickenBurningElementalReaction() {
		super(
			new ElementalReactionSettings("Burning", OriginsGenshin.identifier("burning_quicken"), TextHelper.reaction("reaction.origins-genshin.burning", Colors.PYRO))
				.setReactionCoefficient(0) // Coefficient: 0 since Burning is "special", removes itself when Dendro is gone/by natural causes.
				.setAuraElement(Element.QUICKEN)
				.setTriggeringElement(Element.PYRO, 6)
		);
	}
}
