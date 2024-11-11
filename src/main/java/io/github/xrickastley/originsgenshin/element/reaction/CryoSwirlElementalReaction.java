package io.github.xrickastley.originsgenshin.element.reaction;

import io.github.xrickastley.originsgenshin.OriginsGenshin;
import io.github.xrickastley.originsgenshin.element.Element;

public class CryoSwirlElementalReaction extends AbstractSwirlElementalReaction {
	public CryoSwirlElementalReaction() {
		super(
			new ElementalReactionSettings("Swirl", OriginsGenshin.identifier("swirl_cryo"), null)
				.setReactionCoefficient(0.5)
				.setAuraElement(Element.CRYO, 4)
				.setTriggeringElement(Element.ANEMO, 3)
				.setAsReversable(true)
		);
	}
}
