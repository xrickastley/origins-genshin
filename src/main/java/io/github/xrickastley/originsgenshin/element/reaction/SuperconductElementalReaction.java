package io.github.xrickastley.originsgenshin.element.reaction;

import io.github.xrickastley.originsgenshin.OriginsGenshin;
import io.github.xrickastley.originsgenshin.element.Element;
import io.github.xrickastley.originsgenshin.factory.OriginsGenshinParticleFactory;

public final class SuperconductElementalReaction extends AbstractSuperconductElementalReaction {
	public SuperconductElementalReaction() {
		super(
			new ElementalReactionSettings("Superconduct", OriginsGenshin.identifier("superconduct"), OriginsGenshinParticleFactory.SUPERCONDUCT)
				.setReactionCoefficient(1.0)
				.setAuraElement(Element.CRYO, 1)
				.setTriggeringElement(Element.ELECTRO, 6)
				.reversable(true)
		);
	}
}
