package io.github.xrickastley.originsgenshin.element.reaction;

import io.github.xrickastley.originsgenshin.OriginsGenshin;
import io.github.xrickastley.originsgenshin.element.Element;
import io.github.xrickastley.originsgenshin.factory.OriginsGenshinParticleFactory;

public class BurningElementalReaction extends AbstractBurningElementalReaction {
	public BurningElementalReaction() {
		super(
			new ElementalReactionSettings("Burning", OriginsGenshin.identifier("burning"), OriginsGenshinParticleFactory.BURNING)
				.setReactionCoefficient(0) // Coefficient: 0 since Burning is "special", removes itself when Dendro is gone/by natural causes.
				.setAuraElement(Element.DENDRO, 3)
				.setTriggeringElement(Element.PYRO, 6)
				.applyResultAsAura(true)
				.reversable(true)
		);
	}
}
