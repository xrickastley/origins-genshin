package io.github.xrickastley.originsgenshin.element.reaction;

import io.github.xrickastley.originsgenshin.OriginsGenshin;
import io.github.xrickastley.originsgenshin.element.Element;
import io.github.xrickastley.originsgenshin.factory.OriginsGenshinParticleFactory;

public final class ElectroCrystallizeElementalReaction extends AbstractCrystallizeElementalReaction {
	protected ElectroCrystallizeElementalReaction() {
		super(
			new ElementalReactionSettings("Crystallize", OriginsGenshin.identifier("crystallize_electro"), OriginsGenshinParticleFactory.CRYSTALLIZE)
				.setReactionCoefficient(0.5)
				.setAuraElement(Element.ELECTRO)
				.setTriggeringElement(Element.GEO, 2)
		);
	}
}
