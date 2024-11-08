package io.github.xrickastley.originsgenshin.elements.reactions;

import io.github.xrickastley.originsgenshin.OriginsGenshin;
import io.github.xrickastley.originsgenshin.components.ElementComponent;
import io.github.xrickastley.originsgenshin.elements.Element;
import io.github.xrickastley.originsgenshin.elements.ElementalApplication;
import io.github.xrickastley.originsgenshin.elements.ElementalDamageSource;
import io.github.xrickastley.originsgenshin.factories.OriginsGenshinParticleFactory;

import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;

public class ElectroChargedElementalReaction extends ElementalReaction {
	public ElectroChargedElementalReaction() {
		super(
			new ElementalReactionSettings("Electro-Charged", OriginsGenshin.identifier("electro-charged"), OriginsGenshinParticleFactory.ElectroCharged)
				.setReactionCoefficient(0)
				.setAuraElement(Element.HYDRO, 6)
				.setTriggeringElement(Element.ELECTRO, 5)
				.setAsReversable(true)
		);
	}

	@Override
	public boolean isTriggerable(LivingEntity entity) {
		final ElementComponent component = ElementComponent.KEY.get(entity);

		// We only need both Elements to exist for Electro-Charged.
		return component.hasElementalApplication(auraElement.getLeft()) 
			|| component.hasElementalApplication(triggeringElement.getLeft());
	}

	@Override
	public boolean trigger(LivingEntity entity) {
		if (!isTriggerable(entity)) return false;

		final ElementComponent component = ElementComponent.KEY.get(entity);
		final ElementalApplication auraElement = component.getElementalApplication(this.auraElement.getLeft());
		final ElementalApplication triggeringElement = component.getElementalApplication(this.triggeringElement.getLeft());

		final double reducedGauge = auraElement.reduceGauge(0.4);
		triggeringElement.reduceGauge(reducedGauge);

		this.onReaction(entity, auraElement, triggeringElement, reducedGauge);

		return true;
	}

	@Override
	protected void onReaction(LivingEntity entity, ElementalApplication auraElement, ElementalApplication triggeringElement, double reducedGauge) {
		final double radius = 2.5;
		final World world = entity.getWorld();

		final ElementalApplication application = ElementalApplication.usingGaugeUnits(entity, Element.ELECTRO, 0);
		final ElementalDamageSource source = new ElementalDamageSource(world.getDamageSources().generic(), application, "reactions:electro-charged");

		final float ElectroChargedDMG = (float) (OriginsGenshin.getLevelMultiplier(world) * 1.2);

		for (LivingEntity target : world.getNonSpectatingEntities(LivingEntity.class, Box.of(entity.getLerpedPos(1f), radius * 2, radius * 2, radius * 2))) {
			final boolean inCircleRadius = entity.squaredDistanceTo(target) <= (radius * radius);
			final ElementComponent component = ElementComponent.KEY.get(entity);

			if (inCircleRadius && component.hasElementalApplication(Element.HYDRO)) target.damage(source, ElectroChargedDMG);
		}
	}
}
