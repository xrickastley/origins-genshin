package io.github.xrickastley.originsgenshin.element.reaction;

import io.github.xrickastley.originsgenshin.OriginsGenshin;
import io.github.xrickastley.originsgenshin.element.Element;
import io.github.xrickastley.originsgenshin.factory.OriginsGenshinParticleFactory;

public final class HydroBloomElementalReaction extends AbstractBloomElementalReaction {
	public HydroBloomElementalReaction() {
		super(
			new ElementalReactionSettings("Bloom (Hydro)", OriginsGenshin.identifier("bloom_hydro"), OriginsGenshinParticleFactory.BLOOM)
				.setReactionCoefficient(0.5)
				.setAuraElement(Element.DENDRO)
				.setTriggeringElement(Element.HYDRO, 4)
		);
	}
}
