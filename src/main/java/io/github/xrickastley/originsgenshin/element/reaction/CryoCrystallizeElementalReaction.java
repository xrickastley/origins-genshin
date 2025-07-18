package io.github.xrickastley.originsgenshin.element.reaction;

import io.github.xrickastley.originsgenshin.OriginsGenshin;
import io.github.xrickastley.originsgenshin.element.Element;
import io.github.xrickastley.originsgenshin.factory.OriginsGenshinParticleFactory;

public final class CryoCrystallizeElementalReaction extends AbstractCrystallizeElementalReaction {
	protected CryoCrystallizeElementalReaction() {
		super(
			new ElementalReactionSettings("Crystallize", OriginsGenshin.identifier("crystallize_cryo"), OriginsGenshinParticleFactory.CRYSTALLIZE)
				.setReactionCoefficient(0.5)
				.setAuraElement(Element.CRYO)
				.setTriggeringElement(Element.GEO, 5)
		);
	}
}
