package io.github.xrickastley.originsgenshin.element.reaction;

import io.github.xrickastley.originsgenshin.OriginsGenshin;
import io.github.xrickastley.originsgenshin.element.Element;
import io.github.xrickastley.originsgenshin.factory.OriginsGenshinParticleFactory;

public final class QuickenBurningElementalReaction extends AbstractBurningElementalReaction {
	public QuickenBurningElementalReaction() {
		super(
			new ElementalReactionSettings("Burning (Quicken)", OriginsGenshin.identifier("burning_quicken"), OriginsGenshinParticleFactory.BURNING)
				.setReactionCoefficient(0) // Coefficient: 0 since Burning is "special", removes itself when Dendro is gone/by natural causes.
				.setAuraElement(Element.QUICKEN, 1)
				.setTriggeringElement(Element.PYRO)
		);
	}
}
