package io.github.xrickastley.originsgenshin.element.reaction;

import io.github.xrickastley.originsgenshin.OriginsGenshin;
import io.github.xrickastley.originsgenshin.element.Element;
import io.github.xrickastley.originsgenshin.factory.OriginsGenshinParticleFactory;

public abstract sealed class AbstractPyroMeltElementalReaction 
	extends AmplifyingElementalReaction 
	permits PyroCryoMeltElementalReaction, PyroFrozenMeltElementalReaction
{
	protected AbstractPyroMeltElementalReaction(String name, String idPath, Element auraElement, int elementPriority) {
		super(
			new ElementalReactionSettings(name, OriginsGenshin.identifier(idPath), OriginsGenshinParticleFactory.MELT)
				.setReactionCoefficient(2.0)
				.setAuraElement(auraElement, elementPriority)
				.setTriggeringElement(Element.PYRO),
			2
		);
	}
}
