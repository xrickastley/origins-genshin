package io.github.xrickastley.originsgenshin.element.reaction;

import javax.annotation.Nullable;

import io.github.xrickastley.originsgenshin.OriginsGenshin;
import io.github.xrickastley.originsgenshin.component.ElementComponent;
import io.github.xrickastley.originsgenshin.element.Element;
import io.github.xrickastley.originsgenshin.element.ElementalApplication;
import io.github.xrickastley.originsgenshin.element.ElementalDamageSource;
import io.github.xrickastley.originsgenshin.element.InternalCooldownContext;
import io.github.xrickastley.originsgenshin.events.ReactionTriggered;
import io.github.xrickastley.originsgenshin.factory.OriginsGenshinParticleFactory;
import io.github.xrickastley.originsgenshin.registry.OriginsGenshinDamageTypes;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;

public class ElectroChargedElementalReaction extends ElementalReaction {
	public ElectroChargedElementalReaction() {
		super(
			new ElementalReactionSettings("Electro-Charged", OriginsGenshin.identifier("electro-charged"), OriginsGenshinParticleFactory.ELECTRO_CHARGED)
				.setReactionCoefficient(0)
				.setAuraElement(Element.HYDRO, 6)
				.setTriggeringElement(Element.ELECTRO, 5)
				.applyResultAsAura(true)
				.reversable(true)
		);
	}

	@Override
	public boolean isTriggerable(LivingEntity entity) {
		final ElementComponent component = ElementComponent.KEY.get(entity);

		final ElementalApplication applicationAE = component.getElementalApplication(auraElement.getLeft());
		final ElementalApplication applicationTE = component.getElementalApplication(triggeringElement.getLeft());

		// We need both Elements to exist for Electro-Charged.
		return applicationAE != null && !applicationAE.isEmpty()
			&& applicationTE != null && !applicationTE.isEmpty()
			&& !component.isElectroChargedOnCD();
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
		this.displayReaction(entity);

		ReactionTriggered.EVENT
			.invoker()
			.onReactionTriggered(this, reducedGauge, entity, origin);

		return true;
	}

	@Override
	protected void onReaction(LivingEntity entity, ElementalApplication auraElement, ElementalApplication triggeringElement, double reducedGauge, @Nullable LivingEntity origin) {
		final double radius = 2.5;
		final World world = entity.getWorld();

		final ElementComponent entityComponent = ElementComponent.KEY.get(entity);
		final float ElectroChargedDMG = ElementalReaction.getReactionDamage(world, 1.2f);

		if (origin == null) entityComponent.setElectroChargedOrigin(origin);
		
		for (LivingEntity target : world.getNonSpectatingEntities(LivingEntity.class, Box.of(entity.getLerpedPos(1f), radius * 2, radius * 2, radius * 2))) {
			final boolean inCircleRadius = entity.squaredDistanceTo(target) <= (radius * radius);
			final ElementComponent targetComponent = ElementComponent.KEY.get(target);

			if (inCircleRadius && targetComponent.hasElementalApplication(Element.HYDRO) && !targetComponent.isElectroChargedOnCD()) {
				final ElementalApplication application = ElementalApplication.gaugeUnits(target, Element.ELECTRO, 0);
				final ElementalDamageSource source = new ElementalDamageSource(
					world
						.getDamageSources()
						.create(OriginsGenshinDamageTypes.ELECTRO_CHARGED, entity, origin), 
					application, 
					InternalCooldownContext.ofNone(origin)
				);

				target.damage(source, ElectroChargedDMG);
				
				targetComponent.resetElectroChargedCD();
			}
		}
		
		entityComponent.resetElectroChargedCD();
	}

	// These "mixins" are injected pieces of code (likening @Inject) that allow Burning to work properly, and allow others to easily see the way it was hardcoded.
	public static void mixin$tick(LivingEntity entity) {
		if (!ElementalReactions.ELECTRO_CHARGED.isTriggerable(entity) || entity.getWorld().isClient) return;

		OriginsGenshin
			.sublogger("LivingEntityMixin")
			.info("Electro-Charged - isTriggerable: {} | Hydro: {} | Electro: {}", ElementalReactions.ELECTRO_CHARGED.isTriggerable(entity), ElementComponent.KEY.get(entity).getElementalApplication(Element.HYDRO), ElementComponent.KEY.get(entity).getElementalApplication(Element.ELECTRO));

		ElementalReactions.ELECTRO_CHARGED.trigger(entity);
		
		ElementComponent.sync(entity);
	}

}
