package io.github.xrickastley.originsgenshin.element.reaction;

import io.github.xrickastley.originsgenshin.OriginsGenshin;
import io.github.xrickastley.originsgenshin.element.Element;
import io.github.xrickastley.originsgenshin.factory.OriginsGenshinParticleFactory;

public class DendroBloomElementalReaction extends AbstractBloomElementalReaction {
	public DendroBloomElementalReaction() {
		super(
			new ElementalReactionSettings("Bloom", OriginsGenshin.identifier("bloom_dendro"), OriginsGenshinParticleFactory.Bloom)
				.setReactionCoefficient(2)
				.setAuraElement(Element.HYDRO, 4)
				.setTriggeringElement(Element.DENDRO)
		);
	}
}
