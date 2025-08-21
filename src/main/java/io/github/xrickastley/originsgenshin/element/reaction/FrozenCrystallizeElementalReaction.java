package io.github.xrickastley.originsgenshin.element.reaction;

import io.github.xrickastley.originsgenshin.OriginsGenshin;
import io.github.xrickastley.originsgenshin.element.Element;
import io.github.xrickastley.originsgenshin.factory.OriginsGenshinParticleFactory;

public final class FrozenCrystallizeElementalReaction extends AbstractCrystallizeElementalReaction {
	FrozenCrystallizeElementalReaction() {
		super(
			new ElementalReactionSettings("Crystallize", OriginsGenshin.identifier("crystallize_frozen"), OriginsGenshinParticleFactory.CRYSTALLIZE)
				.setReactionCoefficient(0.5)
				.setAuraElement(Element.FREEZE)
				.setTriggeringElement(Element.GEO, 6)
		);
	}
}
