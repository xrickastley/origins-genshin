package io.github.xrickastley.originsgenshin.element.reaction;

import io.github.xrickastley.originsgenshin.OriginsGenshin;
import io.github.xrickastley.originsgenshin.element.Element;
import io.github.xrickastley.originsgenshin.factory.OriginsGenshinParticleFactory;

public final class CryoMeltElementalReaction extends AmplifyingElementalReaction {
	CryoMeltElementalReaction() {
		super(
			new ElementalReactionSettings("Melt", OriginsGenshin.identifier("melt_cryo"), OriginsGenshinParticleFactory.MELT)
				.setReactionCoefficient(0.5)
				.setAuraElement(Element.PYRO)
				.setTriggeringElement(Element.CRYO, 2),
			1.5
		);
	}
}
