package io.github.xrickastley.originsgenshin.element.reaction;

import io.github.xrickastley.originsgenshin.OriginsGenshin;
import io.github.xrickastley.originsgenshin.element.Element;
import io.github.xrickastley.originsgenshin.factory.OriginsGenshinParticleFactory;

public class HydroSwirlElementalReaction extends AbstractSwirlElementalReaction {
	public HydroSwirlElementalReaction() {
		super(
			new ElementalReactionSettings("Swirl", OriginsGenshin.identifier("swirl_hydro"), OriginsGenshinParticleFactory.SWIRL)
				.setReactionCoefficient(0.5)
				.setAuraElement(Element.HYDRO, 2)
				.setTriggeringElement(Element.ANEMO, 3)
				.setAsReversable(true)
		);
	}
}
