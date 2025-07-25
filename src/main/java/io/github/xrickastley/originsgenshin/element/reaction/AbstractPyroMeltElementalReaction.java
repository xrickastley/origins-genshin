package io.github.xrickastley.originsgenshin.element.reaction;

import io.github.xrickastley.originsgenshin.OriginsGenshin;
import io.github.xrickastley.originsgenshin.element.Element;
import io.github.xrickastley.originsgenshin.factory.OriginsGenshinParticleFactory;

public abstract sealed class AbstractPyroMeltElementalReaction
	extends AmplifyingElementalReaction
	permits PyroCryoMeltElementalReaction, PyroFrozenMeltElementalReaction
{
	AbstractPyroMeltElementalReaction(String name, String idPath, Element auraElement) {
		super(
			new ElementalReactionSettings(name, OriginsGenshin.identifier(idPath), OriginsGenshinParticleFactory.MELT)
				.setReactionCoefficient(2.0)
				.setAuraElement(auraElement)
				.setTriggeringElement(Element.PYRO, 5),
			2
		);
	}
}
