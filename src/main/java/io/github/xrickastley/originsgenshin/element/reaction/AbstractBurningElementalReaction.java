package io.github.xrickastley.originsgenshin.element.reaction;

import java.util.Comparator;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import com.google.common.base.Suppliers;

import io.github.xrickastley.originsgenshin.OriginsGenshin;
import io.github.xrickastley.originsgenshin.component.ElementComponent;
import io.github.xrickastley.originsgenshin.element.Element;
import io.github.xrickastley.originsgenshin.element.ElementalApplication;
import io.github.xrickastley.originsgenshin.element.ElementalDamageSource;
import io.github.xrickastley.originsgenshin.element.InternalCooldownContext;
import io.github.xrickastley.originsgenshin.element.InternalCooldownType;
import io.github.xrickastley.originsgenshin.events.ElementRemoved;
import io.github.xrickastley.originsgenshin.events.ReactionTriggered;
import io.github.xrickastley.originsgenshin.registry.OriginsGenshinDamageTypes;
import io.github.xrickastley.originsgenshin.registry.OriginsGenshinRegistries;
import net.minecraft.entity.LivingEntity;
import net.minecraft.registry.entry.RegistryEntry.Reference;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;

public abstract class AbstractBurningElementalReaction extends ElementalReaction {
	private static final InternalCooldownType BURNING_PYRO_ICD = InternalCooldownType.registered(OriginsGenshin.identifier("reactions/burning/pyro_icd"), 40, 3);
	private static final Supplier<Set<ElementalReaction>> REACTIONS = Suppliers.memoize(
		() -> OriginsGenshinRegistries.ELEMENTAL_REACTION
			.streamEntries()
			.map(Reference::value)
			.filter(r -> !(r.getAuraElement() == Element.PYRO || (r.getTriggeringElement() == Element.PYRO && r.reversable)))
			.collect(Collectors.toSet())
	);

	protected AbstractBurningElementalReaction(ElementalReactionSettings settings) {
		super(settings);

		// Reactions against the Burning aura consume both Burning and Pyro *equally*
	}

	public static void tick(LivingEntity entity) {
		final ElementComponent component = ElementComponent.KEY.get(entity);

		if (!component.hasElementalApplication(Element.BURNING) || component.isBurningOnCD() || entity.getWorld().isClient) return;

		final World world = entity.getWorld();
		final ElementComponent entityComponent = ElementComponent.KEY.get(entity);
		// TODO: Burning DMG from this point (of reapplication) will be calculated based on the stats of the character responsible for the latest instance of Dendro or Pyro application.
		final float burningDMG = OriginsGenshin.getLevelMultiplier(world) * 0.25f;

		for (LivingEntity target : world.getNonSpectatingEntities(LivingEntity.class, Box.of(entity.getLerpedPos(1f), 2, 2, 2))) {
			final boolean inCircleRadius = entity.squaredDistanceTo(target) <= 1;
			final ElementComponent targetComponent = ElementComponent.KEY.get(target);

			if (!inCircleRadius || targetComponent.isBurningOnCD()) continue;
			
			final ElementalApplication application = ElementalApplication.gaugeUnits(target, Element.PYRO,1);
			final ElementalDamageSource source = new ElementalDamageSource(
				world
					.getDamageSources()
					.create(OriginsGenshinDamageTypes.BURNING, entity, entityComponent.getBurningOrigin()), 
				application, 
				InternalCooldownContext.ofType(entity, "origins-genshin:reactions/burning", BURNING_PYRO_ICD)
			);

			target.damage(source, burningDMG);
			target.setOnFire(true);
			target.setFireTicks(5);

			targetComponent.resetBurningCD();
		}

		entityComponent.resetBurningCD();
	}

	@Override
	public boolean isTriggerable(LivingEntity entity) {
		return super.isTriggerable(entity) && !ElementComponent.KEY.get(entity).hasElementalApplication(Element.BURNING);
	}

	@Override
	protected void onReaction(LivingEntity entity, ElementalApplication auraElement, ElementalApplication triggeringElement, double reducedGauge, @Nullable LivingEntity origin) {
		final ElementComponent component = ElementComponent.KEY.get(entity);

		component
			.getElementHolder(Element.BURNING)
			.setElementalApplication(
				ElementalApplication.gaugeUnits(entity, Element.BURNING, 2.0f, false)
			);

		component.resetBurningCD();
		component.setBurningOrigin(origin);

		ElementComponent.sync(entity);
	}

	static {
		ElementRemoved.EVENT.register(application -> {
			if (application == null || (application.getElement() != Element.DENDRO && application.getElement() != Element.QUICKEN)) return;

			final ElementComponent component = ElementComponent.KEY.get(application.getEntity());

			component
				.getElementHolder(Element.BURNING)
				.setElementalApplication(null);

			ElementComponent.sync(application.getEntity());
		});

		ReactionTriggered.EVENT.register((reaction, reducedGauge, target, origin) -> {
			final ElementComponent component = ElementComponent.KEY.get(target);

			if (!component.hasElementalApplication(Element.BURNING) || reaction instanceof AbstractBurningElementalReaction) return;

			final ElementalApplication applicationAE = component.getElementalApplication(reaction.getAuraElement());
			final ElementalApplication applicationTE = component.getElementalApplication(reaction.getTriggeringElement());

			final double newReducedGauge = (applicationAE.getElement() == Element.PYRO
				? applicationTE.getCurrentGauge() + reducedGauge
				: applicationAE.getCurrentGauge() + reducedGauge) * reaction.reactionCoefficient;

			OriginsGenshin
				.sublogger(AbstractBurningElementalReaction.class)
				.info("Reaction: {} | Aura element: {}U {} | Triggering element: {}U {} | Reduced gauge (new): {}", reaction.getId(), applicationAE.getCurrentGauge() + reducedGauge, applicationAE.getElement(), applicationTE.getCurrentGauge() + reducedGauge, applicationTE.getElement(), newReducedGauge);

			component
				.getElementalApplication(Element.BURNING)
				.reduceGauge(newReducedGauge);

			ElementComponent.sync(target);
		});
	}

	// These "mixins" are injected pieces of code (likening @Inject) that allow Burning to work properly.
	public static void mixin$reduceBurningGauge(ElementalApplication auraElement, ElementalApplication triggeringElement, LivingEntity entity, double reducedGauge) {
		if (auraElement.getElement() != Element.PYRO || triggeringElement.getElement() != Element.PYRO) return;

		final ElementComponent component = ElementComponent.KEY.get(entity);

		if (!component.hasElementalApplication(Element.BURNING)) return;

		component
			.getElementHolder(Element.BURNING)
			.getElementalApplication()
			.reduceGauge(reducedGauge);
	}

	public static boolean mixin$onlyAllowPyroReactions(final boolean original, final ElementComponent component, final ElementalReaction reaction) {
		if (!component.hasElementalApplication(Element.BURNING)) return original;

		return original && !REACTIONS.get().contains(reaction);
	}

	public static boolean mixin$allowDendroPassthrough(final boolean original, final ElementComponent component, ElementalApplication application) {
		return original 
			&& !(component.hasElementalApplication(Element.BURNING) && (application.getElement() == Element.DENDRO || application.getElement() == Element.PYRO));
	}

	public static void mixin$reduceQuickenGauge(final ElementalApplication application) {
		final ElementComponent component = ElementComponent.KEY.get(application.getEntity());

		if (!component.hasElementalApplication(Element.BURNING) || application.getElement() != Element.QUICKEN) return;

		application.reduceGauge(Element.DENDRO.getCustomDecayRate().apply(application).doubleValue());
	}

	public static Optional<ElementalReaction> mixin$changeReaction(Optional<ElementalReaction> original, final ElementComponent component) {
		if (!component.hasElementalApplication(Element.BURNING)) return original;
		
		return OriginsGenshinRegistries.ELEMENTAL_REACTION
			.streamEntries()
			.map(Reference::value)
			.filter(r -> r.isTriggerable(component.getOwner()) && (r.getAuraElement() == Element.PYRO || (r.getTriggeringElement() == Element.PYRO && r.reversable)))
			.sorted(Comparator.comparing(r -> r.getPriority(component.getOwner())))
			.findFirst();
	}
}
