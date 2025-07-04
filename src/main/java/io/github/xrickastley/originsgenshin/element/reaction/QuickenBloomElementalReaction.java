package io.github.xrickastley.originsgenshin.element.reaction;

import io.github.xrickastley.originsgenshin.OriginsGenshin;
import io.github.xrickastley.originsgenshin.element.Element;
import io.github.xrickastley.originsgenshin.factory.OriginsGenshinParticleFactory;

public class QuickenBloomElementalReaction extends AbstractBloomElementalReaction {
	public QuickenBloomElementalReaction() {
		super(
			new ElementalReactionSettings("Bloom (Quicken)", OriginsGenshin.identifier("bloom_quicken"), OriginsGenshinParticleFactory.BLOOM)
				.setReactionCoefficient(2.0)
				.setAuraElement(Element.QUICKEN, 2)
				.setTriggeringElement(Element.HYDRO)
		);
	}
}
