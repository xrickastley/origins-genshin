package io.github.xrickastley.originsgenshin.elements.reactions;

import io.github.xrickastley.originsgenshin.OriginsGenshin;
import io.github.xrickastley.originsgenshin.elements.Element;
import io.github.xrickastley.originsgenshin.factories.OriginsGenshinParticleFactory;

public final class CryoMeltElementalReaction extends AmplifyingElementalReaction {
	protected CryoMeltElementalReaction() {
		super(
			new ElementalReactionSettings("melt", OriginsGenshin.identifier("melt_cryo"), OriginsGenshinParticleFactory.Vaporize)
				.setReactionCoefficient(0.5)
				.setAuraElement(Element.PYRO, 2)
				.setTriggeringElement(Element.CRYO),
			1.5
		);
	}
}
