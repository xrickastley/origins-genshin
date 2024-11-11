package io.github.xrickastley.originsgenshin.element.reaction;

import io.github.xrickastley.originsgenshin.OriginsGenshin;
import io.github.xrickastley.originsgenshin.element.Element;
import io.github.xrickastley.originsgenshin.factory.OriginsGenshinParticleFactory;

public final class CryoMeltElementalReaction extends AmplifyingElementalReaction {
	protected CryoMeltElementalReaction() {
		super(
			new ElementalReactionSettings("Melt", OriginsGenshin.identifier("melt_cryo"), OriginsGenshinParticleFactory.Vaporize)
				.setReactionCoefficient(0.5)
				.setAuraElement(Element.PYRO, 2)
				.setTriggeringElement(Element.CRYO),
			1.5
		);
	}
}
