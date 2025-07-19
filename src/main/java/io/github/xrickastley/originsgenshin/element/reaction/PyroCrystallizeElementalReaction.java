package io.github.xrickastley.originsgenshin.element.reaction;

import io.github.xrickastley.originsgenshin.OriginsGenshin;
import io.github.xrickastley.originsgenshin.element.Element;
import io.github.xrickastley.originsgenshin.factory.OriginsGenshinParticleFactory;

public final class PyroCrystallizeElementalReaction extends AbstractCrystallizeElementalReaction {
	PyroCrystallizeElementalReaction() {
		super(
			new ElementalReactionSettings("Crystallize", OriginsGenshin.identifier("crystallize_pyro"), OriginsGenshinParticleFactory.CRYSTALLIZE)
				.setReactionCoefficient(0.5)
				.setAuraElement(Element.PYRO)
				.setTriggeringElement(Element.GEO, 3)
		);
	}
}
