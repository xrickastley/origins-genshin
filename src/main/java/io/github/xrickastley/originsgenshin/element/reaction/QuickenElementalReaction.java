package io.github.xrickastley.originsgenshin.element.reaction;

import io.github.xrickastley.originsgenshin.OriginsGenshin;
import io.github.xrickastley.originsgenshin.component.ElementComponent;
import io.github.xrickastley.originsgenshin.element.Element;
import io.github.xrickastley.originsgenshin.element.ElementalApplication;
import io.github.xrickastley.originsgenshin.element.ElementalApplications;
import io.github.xrickastley.originsgenshin.util.TextHelper;

import net.minecraft.entity.LivingEntity;

import javax.annotation.Nullable;

public final class QuickenElementalReaction extends ElementalReaction {
	QuickenElementalReaction() {
		super(
			new ElementalReactionSettings("Quicken", OriginsGenshin.identifier("quicken"), TextHelper.reaction("reaction.origins-genshin.quicken", "#01e858"))
				.setReactionCoefficient(1.0)
				.setAuraElement(Element.DENDRO, 2)
				.setTriggeringElement(Element.ELECTRO, 8)
				.reversable(true)
				.preventsPriorityUpgrade(true)
				.preventsReactionsAfter(OriginsGenshin.identifier("spread"), OriginsGenshin.identifier("aggravate"))
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
	}

	// These "mixins" are injected pieces of code (likening @Inject) that allow Burning to work properly, and allow others to easily see the way it was hardcoded.
	public static boolean mixin$preventReapplication(ElementalApplication application, ElementComponent component) {
		return (application.getElement() == Element.ELECTRO || application.getElement() == Element.DENDRO)
			&& component.hasElementalApplication(Element.QUICKEN);
	}
}
