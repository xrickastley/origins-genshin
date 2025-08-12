package io.github.xrickastley.originsgenshin.element.reaction;

import com.google.common.base.Suppliers;

import java.util.Comparator;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import io.github.xrickastley.originsgenshin.OriginsGenshin;
import io.github.xrickastley.originsgenshin.component.ElementComponent;
import io.github.xrickastley.originsgenshin.element.Element;
import io.github.xrickastley.originsgenshin.element.ElementHolder;
import io.github.xrickastley.originsgenshin.element.ElementalApplication;
import io.github.xrickastley.originsgenshin.element.ElementalApplications;
import io.github.xrickastley.originsgenshin.element.ElementalDamageSource;
import io.github.xrickastley.originsgenshin.element.InternalCooldownContext;
import io.github.xrickastley.originsgenshin.element.InternalCooldownType;
import io.github.xrickastley.originsgenshin.events.ElementApplied;
import io.github.xrickastley.originsgenshin.events.ElementRemoved;
import io.github.xrickastley.originsgenshin.events.ReactionTriggered;
import io.github.xrickastley.originsgenshin.registry.OriginsGenshinDamageTypes;
import io.github.xrickastley.originsgenshin.registry.OriginsGenshinRegistries;

import net.minecraft.entity.LivingEntity;
import net.minecraft.registry.entry.RegistryEntry.Reference;

import javax.annotation.Nullable;

public abstract sealed class AbstractBurningElementalReaction
	extends ElementalReaction
	permits BurningElementalReaction, QuickenBurningElementalReaction
{
	private static final InternalCooldownType BURNING_PYRO_ICD = InternalCooldownType.registered(OriginsGenshin.identifier("reactions/burning/pyro_icd"), 40, Integer.MAX_VALUE);
	private static final Supplier<Set<ElementalReaction>> REACTIONS = Suppliers.memoize(
		() -> OriginsGenshinRegistries.ELEMENTAL_REACTION
			.streamEntries()
			.map(Reference::value)
			.filter(r -> !(r.getAuraElement() == Element.PYRO || (r.getTriggeringElement() == Element.PYRO && r.reversable)))
			.collect(Collectors.toSet())
	);

	AbstractBurningElementalReaction(ElementalReactionSettings settings) {
		super(settings);
	}

	@Override
	public boolean isTriggerable(LivingEntity entity) {
		return super.isTriggerable(entity) && !ElementComponent.KEY.get(entity).hasElementalApplication(Element.BURNING);
	}

	@Override
	protected void onReaction(LivingEntity entity, ElementalApplication auraElement, ElementalApplication triggeringElement, double reducedGauge, @Nullable LivingEntity origin) {
		final ElementComponent component = ElementComponent.KEY.get(entity);
		final InternalCooldownContext context = InternalCooldownContext.ofType(origin, "origins-genshin:reactions/burning", BURNING_PYRO_ICD);
		final ElementHolder holder = component.getElementHolder(Element.PYRO);

		if (context.hasOrigin() && context.getInternalCooldown(holder).handleInternalCooldown()) {
			final ElementalApplication application = ElementalApplications.gaugeUnits(entity, Element.PYRO, 1.0f, true);

			if (!holder.hasElementalApplication() || holder.getElementalApplication().isEmpty()) {
				holder.setElementalApplication(application);
			} else {
				holder.getElementalApplication().reapply(application);
			}
		}

		component
			.getElementHolder(Element.BURNING)
			.setElementalApplication(
				ElementalApplications.gaugeUnits(entity, Element.BURNING, 2.0f, true)
			);

		component.resetBurningCD();
		component.setOrRetainBurningOrigin(origin);

		ElementComponent.sync(entity);
	}

	static {
		ElementApplied.EVENT.register(application -> {
			if (application.getEntity().getWorld().isClient || application.getElement() != Element.BURNING) return;

			final ElementComponent component = ElementComponent.KEY.get(application.getEntity());

			if (component.hasElementalApplication(Element.DENDRO) || component.hasElementalApplication(Element.QUICKEN)) return;

			component
				.getElementHolder(Element.BURNING)
				.setElementalApplication(null);

			ElementComponent.sync(application.getEntity());
		});

		ElementRemoved.EVENT.register(application -> {
			if (application.getElement() == Element.BURNING) ElementComponent.sync(application.getEntity());

			if (application.getElement() != Element.DENDRO && application.getElement() != Element.QUICKEN) return;

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

			component
				.getElementalApplication(Element.BURNING)
				.reduceGauge(newReducedGauge);

			ElementComponent.sync(target);
		});
	}

	// These "mixins" are injected pieces of code (likening @Inject) that allow Burning to work properly, and allow others to easily see the way it was hardcoded.
	public static void mixin$tick(LivingEntity entity) {
		final ElementComponent component = ElementComponent.KEY.get(entity);

		if (!component.hasElementalApplication(Element.BURNING) || component.isBurningOnCD() || entity.getWorld().isClient) return;

		if (!component.hasElementalApplication(Element.DENDRO) && !component.hasElementalApplication(Element.QUICKEN)) {
			component
				.getElementHolder(Element.BURNING)
				.setElementalApplication(null);

			return;
		}

		for (final LivingEntity target : ElementalReaction.getEntitiesInAoE(entity, 1, t -> !ElementComponent.KEY.get(t).isBurningOnCD())) {
			// TODO: Burning DMG from this point (of reapplication) will be calculated based on the stats of the character responsible for the latest instance of Dendro or Pyro application. (not possible unless EM is added, there is no EM here)
			final float damage = ElementalReaction.getReactionDamage(entity, 0.25);
			final ElementalDamageSource source = new ElementalDamageSource(
				entity
					.getDamageSources()
					.create(OriginsGenshinDamageTypes.BURNING, entity, component.getBurningOrigin()),
				ElementalApplications.gaugeUnits(target, Element.PYRO, 0),
				InternalCooldownContext.ofNone()
			).shouldApplyDMGBonus(false);

			target.damage(source, damage);
			target.setOnFire(true);
			target.setFireTicks(5);

			final ElementComponent targetComponent = ElementComponent.KEY.get(target);
			final ElementHolder holder = targetComponent.getElementHolder(Element.PYRO);
			
			if (holder.canApplyElement(Element.PYRO, InternalCooldownContext.ofType(entity, "origins-genshin:reactions/burning", BURNING_PYRO_ICD), true)) {
				final ElementalApplication application = holder.getElementalApplication();
	
				if (application == null) {
					holder.setElementalApplication(ElementalApplications.gaugeUnits(target, Element.PYRO, 1, true));
				} else {
					application.reapply(Element.PYRO, 1);
				}
			}

			targetComponent.resetBurningCD();
		}
	}

	/**
	 * Reapplies the Dendro element when the only "highest priority" element is Burning. <br> <br>
	 *
	 * This method will <b>overwrite</b> the current Dendro aura with the provided Elemental
	 * Application, as specified by the "Burning Refresh" mechanic by <a href="https://genshin-impact.fandom.com/wiki/Elemental_Gauge_Theory/Advanced_Mechanics#Burning">
	 * Elemental Gauge Theory > Advanced Mechanics > Burning</a>.
	 *
	 * If the provided application is <i>not</i> the Dendro element, it is ignored.
	 *
	 * @param application The {@code ElementalApplication} to reapply.
	 */
	public static void mixin$forceReapplyDendroWhenBurning(ElementComponent component, ElementalApplication application) {
		if (application.getElement() != Element.DENDRO) return;

		final Set<Element> appliedElements = component
			.getAppliedElements()
			.stream()
			.map(ElementalApplication::getElement)
			.collect(Collectors.toSet());

		if (!appliedElements.contains(Element.BURNING)) return;

		component
			.getElementHolder(Element.DENDRO)
			.setElementalApplication(application.asAura());

		ElementComponent.sync(component.getOwner());
	}

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

	public static Optional<ElementalReaction> mixin$changeReaction(Optional<ElementalReaction> original, final ElementComponent component, final ElementalApplication application) {
		if (!component.hasElementalApplication(Element.BURNING)) return original;

		return OriginsGenshinRegistries.ELEMENTAL_REACTION
			.streamEntries()
			.map(Reference::value)
			.filter(r -> r.isTriggerable(component.getOwner()) && (r.getAuraElement() == Element.PYRO || (r.getTriggeringElement() == Element.PYRO && r.reversable)))
			.sorted(Comparator.comparing(r -> r.getPriority(application)))
			.findFirst();
	}
}
