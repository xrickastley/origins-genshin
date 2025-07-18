package io.github.xrickastley.originsgenshin.element.reaction;

import javax.annotation.Nullable;

import io.github.xrickastley.originsgenshin.OriginsGenshin;
import io.github.xrickastley.originsgenshin.component.ElementComponent;
import io.github.xrickastley.originsgenshin.element.Element;
import io.github.xrickastley.originsgenshin.element.ElementalApplication;
import io.github.xrickastley.originsgenshin.element.ElementalApplications;
import io.github.xrickastley.originsgenshin.factory.OriginsGenshinParticleFactory;
import net.minecraft.entity.LivingEntity;

public final class QuickenElementalReaction extends ElementalReaction {
	public QuickenElementalReaction() {
		super(
			new ElementalReactionSettings("Quicken", OriginsGenshin.identifier("quicken"), OriginsGenshinParticleFactory.QUICKEN)
				.setReactionCoefficient(1.0)
				.setAuraElement(Element.DENDRO, 2)
				.setTriggeringElement(Element.ELECTRO, 8)
				.reversable(true)
				.endsReactionTrigger(true)
		);
	}

	@Override
	protected void onReaction(LivingEntity entity, ElementalApplication auraElement, ElementalApplication triggeringElement, double reducedGauge, @Nullable LivingEntity origin) {
		final double quickenAuraGauge = Math.min(auraElement.getCurrentGauge() + reducedGauge, triggeringElement.getCurrentGauge() + reducedGauge);
		final double tickDuration = (quickenAuraGauge * 5 + 6) * 20;

		ElementComponent.KEY
			.get(entity)
			.getElementHolder(Element.QUICKEN)
			.setElementalApplication(
				ElementalApplications.duration(entity, Element.QUICKEN, quickenAuraGauge, tickDuration)
			);

		OriginsGenshin
			.sublogger(this)
			.info("Quicken: {}", ElementComponent.KEY.get(entity).getElementalApplication(Element.QUICKEN));
	}
}
