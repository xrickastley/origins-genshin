package io.github.xrickastley.originsgenshin.element.reaction;

import io.github.xrickastley.originsgenshin.OriginsGenshin;
import io.github.xrickastley.originsgenshin.element.Element;
import io.github.xrickastley.originsgenshin.factory.OriginsGenshinParticleFactory;

public final class DendroBloomElementalReaction extends AbstractBloomElementalReaction {
	DendroBloomElementalReaction() {
		super(
			new ElementalReactionSettings("Bloom", OriginsGenshin.identifier("bloom_dendro"), OriginsGenshinParticleFactory.BLOOM)
				.setReactionCoefficient(2)
				.setAuraElement(Element.HYDRO)
				.setTriggeringElement(Element.DENDRO, 4)
		);
	}
}
