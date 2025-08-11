package io.github.xrickastley.originsgenshin.element.reaction;

import java.text.DecimalFormat;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.slf4j.Logger;

import io.github.xrickastley.originsgenshin.OriginsGenshin;
import io.github.xrickastley.originsgenshin.component.ElementComponent;
import io.github.xrickastley.originsgenshin.element.Element;
import io.github.xrickastley.originsgenshin.element.ElementalApplication;
import io.github.xrickastley.originsgenshin.events.ReactionTriggered;
import io.github.xrickastley.originsgenshin.factory.OriginsGenshinSoundEvents;
import io.github.xrickastley.originsgenshin.networking.ShowElementalReactionS2CPacket;
import io.github.xrickastley.originsgenshin.registry.OriginsGenshinRegistries;
import io.github.xrickastley.originsgenshin.util.Array;

import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.particle.DefaultParticleType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public abstract class ElementalReaction {
	private static final Logger LOGGER = OriginsGenshin.sublogger(ElementalReaction.class);

	protected final String name;
	protected final Identifier id;
	protected final @Nullable DefaultParticleType particle;
	protected final double reactionCoefficient;
	protected final Pair<Element, Integer> auraElement;
	protected final Pair<Element, Integer> triggeringElement;
	protected final boolean reversable;
	@Deprecated
	protected final boolean allowChildElements = false;
	protected final boolean applyResultAsAura;
	protected final boolean endsReactionTrigger;
	protected final boolean preventsPriorityUpgrade;

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
		this.preventsPriorityUpgrade = settings.preventsPriorityUpgrade;

		OriginsGenshinRegistries.ELEMENTAL_REACTION.createEntry(this);
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
		return getEntitiesInAoE(target, radius, e -> true);
	}

	public static List<LivingEntity> getEntitiesInAoE(LivingEntity target, double radius, Predicate<LivingEntity> filter) {
		final List<LivingEntity> targets = target
			.getWorld()
			.getNonSpectatingEntities(LivingEntity.class, Box.of(target.getLerpedPos(1f), radius * 2, radius * 2, radius * 2));

		targets.removeIf(entity -> entity.squaredDistanceTo(target) >= radius * radius || filter.negate().test(entity));

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

	public int getHighestElementPriority() {
		return Math.min(this.auraElement.getLeft().getPriority(), this.triggeringElement.getLeft().getPriority());
	}

	public @Nullable DefaultParticleType getParticle() {
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

	public boolean shouldPreventPriorityUpgrade() {
		return this.preventsPriorityUpgrade;
	}

	/**
	 * Gets the priority of this Elemental Reaction.
	 * @param application The applied Elemental Application, which should be the triggering element.
	 * @return The priority of this Elemental Reaction.
	 */
	public int getPriority(ElementalApplication application) {
		return getPriority(application.getElement());
	}

	/**
	 * Gets the priority of this Elemental Reaction.
	 * @param triggeringElement The applied element, also known as the triggering element.
	 * @return The priority of this Elemental Reaction.
	 */
	public int getPriority(Element triggeringElement) {
		return triggeringElement.equals(this.triggeringElement.getLeft())
			? this.triggeringElement.getRight()
			: triggeringElement.equals(this.auraElement.getLeft()) && this.reversable
				? this.auraElement.getRight()
				:  Integer.MAX_VALUE;
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

		final ElementalApplication auraElement = component.getElementalApplication(this.auraElement.getLeft());
		final ElementalApplication trigElement = component.getElementalApplication(this.triggeringElement.getLeft());

		final boolean result = reversable
			// Any of the elements can be an Aura element.
			? auraElement != null && trigElement != null && !auraElement.isEmpty() && !trigElement.isEmpty()
			// The aura element must STRICTLY be an Aura element.
			: auraElement != null && trigElement != null && auraElement.isAuraElement() && !auraElement.isEmpty() && !trigElement.isEmpty();

		return result;
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

		LOGGER.debug("Phase: BEFORE - Aura element: {} GU {}; Triggering elements: {} GU {}; Reaction coefficient: {}", df.format(applicationAE.getCurrentGauge()), applicationAE.getElement(), df.format(applicationTE.getCurrentGauge()), applicationTE.getElement(), reactionCoefficient);

		final double reducedGauge = applicationAE.reduceGauge(reactionCoefficient * applicationTE.getCurrentGauge());

		LOGGER.debug("Phase: CALCULATE - Reaction coefficient: {} | Reduced Gauge (AE): {}", reactionCoefficient, reducedGauge);

		applicationTE.reduceGauge(reducedGauge);

		LOGGER.debug("Phase: AFTER - Aura element: {} GU {}; Triggering elements: {} GU {}; Reaction coefficient: {}", df.format(applicationAE.getCurrentGauge()), applicationAE.getElement(), df.format(applicationTE.getCurrentGauge()), applicationTE.getElement(), reactionCoefficient);

		AbstractBurningElementalReaction.mixin$reduceBurningGauge(applicationAE, applicationTE, entity, reducedGauge);
		this.onReaction(entity, applicationAE, applicationTE, reducedGauge, origin);
		this.displayReaction(entity);

		ReactionTriggered.EVENT
			.invoker()
			.onReactionTriggered(this, reducedGauge, entity, origin);

		entity
			.getWorld()
			.playSound(null, entity.getBlockPos(), OriginsGenshinSoundEvents.REACTION, SoundCategory.PLAYERS, 1.0f, 1.0f);

		return true;
	}

	public boolean idEquals(ElementalReaction reaction) {
		return this.getId().equals(reaction.getId());
	}

	protected void displayReaction(LivingEntity target) {
		if (target.getWorld().isClient) return;

		final Box boundingBox = target.getBoundingBox();

		final double x = target.getX() + (boundingBox.getLengthX() * 1.25 * Math.random());
		final double y = target.getY() + (boundingBox.getLengthY() * 0.50);
		final double z = target.getZ() + (boundingBox.getLengthZ() * 1.25 * Math.random());

		final Vec3d pos = new Vec3d(x, y, z);

		final ShowElementalReactionS2CPacket packet = new ShowElementalReactionS2CPacket(pos, this.getId());

		if (target instanceof final ServerPlayerEntity serverPlayer) ServerPlayNetworking.send(serverPlayer, packet);

		for (final ServerPlayerEntity otherPlayer : PlayerLookup.tracking(target)) {
			if (otherPlayer.getId() == target.getId()) continue;

			ServerPlayNetworking.send(otherPlayer, packet);
		}
	}
}
