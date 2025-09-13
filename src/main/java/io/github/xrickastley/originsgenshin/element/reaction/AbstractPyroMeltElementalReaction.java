package io.github.xrickastley.originsgenshin.element.reaction;

import io.github.xrickastley.originsgenshin.OriginsGenshin;
import io.github.xrickastley.originsgenshin.element.Element;
import io.github.xrickastley.originsgenshin.util.TextHelper;

public abstract sealed class AbstractPyroMeltElementalReaction
	extends AmplifyingElementalReaction
	permits PyroCryoMeltElementalReaction, PyroFrozenMeltElementalReaction
{
	AbstractPyroMeltElementalReaction(String name, String idPath, Element auraElement) {
		super(
			new ElementalReactionSettings(name, OriginsGenshin.identifier(idPath), TextHelper.reaction("reaction.origins-genshin.melt", "#f2be87"))
				.setReactionCoefficient(2.0)
				.setAuraElement(auraElement)
				.setTriggeringElement(Element.PYRO, 5)
				.preventsReactionsAfter(OriginsGenshin.identifier("vaporize_pyro"), OriginsGenshin.identifier("melt_pyro-frozen")),
			2
		);
	}
}
