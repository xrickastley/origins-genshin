package io.github.xrickastley.originsgenshin.element.reaction;

import javax.annotation.Nullable;

import io.github.xrickastley.originsgenshin.OriginsGenshin;
import io.github.xrickastley.originsgenshin.component.ElementComponent;
import io.github.xrickastley.originsgenshin.element.Element;
import io.github.xrickastley.originsgenshin.element.ElementalApplication;
import io.github.xrickastley.originsgenshin.element.ElementalDamageSource;
import io.github.xrickastley.originsgenshin.factory.OriginsGenshinParticleFactory;
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
	public boolean trigger(LivingEntity entity, @Nullable LivingEntity origin) {
		if (!isTriggerable(entity)) return false;

		final ElementComponent component = ElementComponent.KEY.get(entity);
		final ElementalApplication auraElement = component.getElementalApplication(this.auraElement.getLeft());
		final ElementalApplication triggeringElement = component.getElementalApplication(this.triggeringElement.getLeft());

		final double reducedGauge = auraElement.reduceGauge(0.4);
		triggeringElement.reduceGauge(reducedGauge);

		this.onReaction(entity, auraElement, triggeringElement, reducedGauge, origin);

		return true;
	}

	@Override
	protected void onReaction(LivingEntity entity, ElementalApplication auraElement, ElementalApplication triggeringElement, double reducedGauge, @Nullable LivingEntity origin) {
		final double radius = 2.5;
		final World world = entity.getWorld();

		final float ElectroChargedDMG = OriginsGenshin.getLevelMultiplier(world) * 1.2f;
		
		for (LivingEntity target : world.getNonSpectatingEntities(LivingEntity.class, Box.of(entity.getLerpedPos(1f), radius * 2, radius * 2, radius * 2))) {
			final ElementalApplication application = ElementalApplication.usingGaugeUnits(target, Element.PYRO, 0);
			final ElementalDamageSource source = new ElementalDamageSource(world.getDamageSources().generic(), application, "reactions:overloaded");
			
			final boolean inCircleRadius = entity.squaredDistanceTo(target) <= (radius * radius);
			final ElementComponent component = ElementComponent.KEY.get(target);

			if (inCircleRadius && component.hasElementalApplication(Element.HYDRO)) target.damage(source, ElectroChargedDMG);
		}
	}
}
