package io.github.xrickastley.originsgenshin.element.reaction;

import io.github.xrickastley.originsgenshin.OriginsGenshin;
import io.github.xrickastley.originsgenshin.element.Element;

public class ElectroSwirlElementalReaction extends AbstractSwirlElementalReaction {
	public ElectroSwirlElementalReaction() {
		super(
			new ElementalReactionSettings("Swirl", OriginsGenshin.identifier("swirl_electro"), null)
				.setReactionCoefficient(0.5)
				.setAuraElement(Element.ELECTRO, 4)
				.setTriggeringElement(Element.ANEMO, 1)
				.setAsReversable(true)
		);
	}
}
