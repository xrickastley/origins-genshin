package io.github.xrickastley.originsgenshin.element.reaction;

import java.text.DecimalFormat;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Stream;

import javax.annotation.Nullable;

import org.slf4j.Logger;

import io.github.xrickastley.originsgenshin.util.Array;
import io.github.xrickastley.originsgenshin.OriginsGenshin;
import io.github.xrickastley.originsgenshin.component.ElementComponent;
import io.github.xrickastley.originsgenshin.element.Element;
import io.github.xrickastley.originsgenshin.element.ElementalApplication;
import io.github.xrickastley.originsgenshin.events.ReactionTriggered;
import io.github.xrickastley.originsgenshin.networking.ShowElementalReactionS2CPacket;

import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.particle.DefaultParticleType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;

public abstract class ElementalReaction {
	private static final Logger LOGGER = OriginsGenshin.sublogger(ElementalReaction.class);

	protected final String name;
	protected final Identifier id;
	protected final DefaultParticleType particle;
	protected final double reactionCoefficient;
	protected final Pair<Element, Integer> auraElement;
	protected final Pair<Element, Integer> triggeringElement;
	protected final boolean reversable;
	@Deprecated
	protected final boolean allowChildElements = false;
	protected final boolean applyResultAsAura;
	protected final boolean endsReactionTrigger;
	
	protected ElementalReaction(ElementalReactionSettings settings) {
		this.name = settings.name;
		this.id = settings.id;
		this.particle = settings.particle;

		this.reactionCoefficient = settings.reactionCoefficient;
		this.auraElement = settings.auraElement;
		this.triggeringElement = settings.triggeringElement;
		this.reversable = settings.reversable;
		this.applyResultAsAura = settings.applyResultAsAura;
		this.endsReactionTrigger = settings.endsReactionTrigger;
	}

	public static float getReactionDamage(Entity entity, double reactionMultiplier) {
		return ElementalReaction.getReactionDamage(entity, (float) reactionMultiplier);
	}

	public static float getReactionDamage(Entity entity, float reactionMultiplier) {
		return OriginsGenshin.getLevelMultiplier(entity) * reactionMultiplier;
	}

	public static float getReactionDamage(World world, double reactionMultiplier) {
		return ElementalReaction.getReactionDamage(world, (float) reactionMultiplier);
	}

	public static float getReactionDamage(World world, float reactionMultiplier) {
		return OriginsGenshin.getLevelMultiplier(world) * reactionMultiplier;
	}

	public static List<LivingEntity> getEntitiesInAoE(LivingEntity target, double radius) {
		return getEntitiesInAoE(target, radius, e -> false);
	}

	public static List<LivingEntity> getEntitiesInAoE(LivingEntity target, double radius, Predicate<LivingEntity> filter) {
		final List<LivingEntity> targets = target
			.getWorld()
			.getNonSpectatingEntities(LivingEntity.class, Box.of(target.getLerpedPos(1f), radius * 2, radius * 2, radius * 2));

		targets.removeIf(entity -> entity.squaredDistanceTo(target) <= 1 || filter.negate().test(entity));

		return targets;
	}

	public boolean hasElement(Element element) {
		return element == this.auraElement.getLeft() || element == this.triggeringElement.getLeft();
	}

	public boolean hasAnyElement(Array<Element> elements) {
		return this.hasAnyElement(elements.stream());
	}

	public boolean hasAnyElement(List<Element> elements) {
		return this.hasAnyElement(elements.stream());
	}

	public boolean hasAnyElement(Stream<Element> elements) {
		return elements.anyMatch(this::hasElement);
	}

	public Element getAuraElement() {
		return auraElement.getLeft();
	}

	public Element getTriggeringElement() {
		return triggeringElement.getLeft();
	}

	public int getAuraElementPriority() {
		return auraElement.getRight();
	}

	public int getTriggeringElementPriority() {
		return triggeringElement.getRight();
	}

	public DefaultParticleType getParticle() {
		return this.particle;
	}

	public Pair<Element, Integer> getElementPair(Element element) {
		return element == auraElement.getLeft()
			? auraElement
			: element == triggeringElement.getLeft()
				? triggeringElement
				: null;
	}

	public boolean shouldApplyResultAsAura() {
		return this.applyResultAsAura;
	}

	public boolean shouldEndReactionTrigger() {
		return this.endsReactionTrigger;
	}

	/**
	 * Gets the priority of this Elemental Reaction for a specific living entity.
	 * @param entity The living entity.
	 * @return The priority of this Elemental Reaction for the specified entity.
	 */
	public int getPriority(LivingEntity entity) {
		return ElementComponent.KEY.get(entity)
			.getAppliedElements()
			.filter(application -> auraElement.getLeft().isChild(application.getElement()))
			.findFirst()
			.map(application -> getPriority(application.getElement()))
			.orElse(-1);
	}

	public int getPriority(Element element) {
		return element.equals(auraElement.getLeft())
			? auraElement.getRight()
			: element.equals(triggeringElement.getLeft()) && reversable
				? triggeringElement.getRight()
				: -1;
	}

	/**
	 * The function to execute after the Elemental Reaction has been triggered. This function is executed after both elements have reacted and have been reduced.
	 * @param entity The {@code LivingEntity} this Elemental Reaction was triggered on.
	 * @param auraElement The aura element that triggered this reaction.
	 * @param triggeringElement The triggering element that reacted with the aura element.
	 * @param reducedGauge The gauge units reduced from both Elements. This will always be {@code Math.min(auraElementGU, triggeringElementGU * reactionCoefficient)}
	 * @param origin The {@code LivingEntity} that triggered this Elemental Reaction.
	 */
	protected abstract void onReaction(LivingEntity entity, ElementalApplication auraElement, ElementalApplication triggeringElement, double reducedGauge, @Nullable LivingEntity origin);

	public Identifier getId() {
		return id;
	}

	public boolean isTriggerable(Entity entity) {
		return entity instanceof final LivingEntity livingEntity
			? isTriggerable(livingEntity)
			: false;
	}

	public boolean isTriggerable(LivingEntity entity) {
		final ElementComponent component = ElementComponent.KEY.get(entity);

		ElementalApplication applicationAE = component.getElementalApplication(auraElement.getLeft());
		ElementalApplication applicationTE = component.getElementalApplication(triggeringElement.getLeft());

		return reversable
			// Any of the elements can be an Aura element.
			? applicationAE != null && applicationTE != null && applicationAE.getCurrentGauge() > 0 && applicationTE.getCurrentGauge() > 0
			// The aura element must STRICTLY be an Aura element.
			: applicationAE != null && applicationTE != null && applicationAE.isAuraElement() && applicationAE.getCurrentGauge() > 0 && applicationTE.getCurrentGauge() > 0;
	}

	public boolean trigger(LivingEntity entity) {
		return this.trigger(entity, null);
	}

	public boolean trigger(LivingEntity entity, @Nullable LivingEntity origin) {
		if (!isTriggerable(entity)) return false;

		final ElementComponent component = ElementComponent.KEY.get(entity);
		ElementalApplication applicationAE = component.getElementalApplication(auraElement.getLeft());
		ElementalApplication applicationTE = component.getElementalApplication(triggeringElement.getLeft());

		if (applicationTE.isAuraElement() && !applicationAE.isAuraElement()) {
			ElementalApplication a = applicationTE;
			applicationTE = applicationAE;
			applicationAE = a;
		}

		final DecimalFormat df = new DecimalFormat("0.0");
		
		LOGGER.info("Phase: BEFORE - Aura element: {} GU {}; Triggering elements: {} GU {}; Reaction coefficient: {}", df.format(applicationAE.getCurrentGauge()), applicationAE.getElement(), df.format(applicationTE.getCurrentGauge()), applicationTE.getElement(), reactionCoefficient);

		final double reducedGauge = applicationAE.reduceGauge(reactionCoefficient * applicationTE.getCurrentGauge());

		LOGGER.info("Phase: CALCULATE - Reaction coefficient: {} | Reduced Gauge (AE): {}", reactionCoefficient, reducedGauge);

		applicationTE.reduceGauge(reducedGauge);
		
		LOGGER.info("Phase: AFTER - Aura element: {} GU {}; Triggering elements: {} GU {}; Reaction coefficient: {}", df.format(applicationAE.getCurrentGauge()), applicationAE.getElement(), df.format(applicationTE.getCurrentGauge()), applicationTE.getElement(), reactionCoefficient);

		AbstractBurningElementalReaction.mixin$reduceBurningGauge(applicationAE, applicationTE, entity, reducedGauge);
		this.onReaction(entity, applicationAE, applicationTE, reducedGauge, origin);
		this.displayReaction(entity);

		ReactionTriggered.EVENT
			.invoker()
			.onReactionTriggered(this, reducedGauge, entity, origin);

		return true;
	}

	public boolean idEquals(ElementalReaction reaction) {
		return this.getId().equals(reaction.getId());
	}

	protected void displayReaction(LivingEntity target) {
		if (target.getWorld().isClient()) return;
		
		ShowElementalReactionS2CPacket packet = new ShowElementalReactionS2CPacket(target.getId(), this.getId());

		if (target instanceof final ServerPlayerEntity serverPlayer) ServerPlayNetworking.send(serverPlayer, packet);
		
		for (ServerPlayerEntity otherPlayer : PlayerLookup.tracking(target)) {
			if (otherPlayer.getId() == target.getId()) continue;
			
			ServerPlayNetworking.send(otherPlayer, packet);
		}
	}
}
