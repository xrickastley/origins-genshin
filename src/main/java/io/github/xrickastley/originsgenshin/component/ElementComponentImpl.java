package io.github.xrickastley.originsgenshin.component;

import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

import javax.annotation.Nonnull;

import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import io.github.xrickastley.originsgenshin.util.Array;
import io.github.xrickastley.originsgenshin.OriginsGenshin;
import io.github.xrickastley.originsgenshin.element.Element;
import io.github.xrickastley.originsgenshin.element.ElementHolder;
import io.github.xrickastley.originsgenshin.element.ElementalApplication;
import io.github.xrickastley.originsgenshin.element.ElementalApplications;
import io.github.xrickastley.originsgenshin.element.ElementalDamageSource;
import io.github.xrickastley.originsgenshin.element.InternalCooldownContext;
import io.github.xrickastley.originsgenshin.element.reaction.AbstractBurningElementalReaction;
import io.github.xrickastley.originsgenshin.element.reaction.ElectroChargedElementalReaction;
import io.github.xrickastley.originsgenshin.element.reaction.ElementalReaction;
import io.github.xrickastley.originsgenshin.registry.OriginsGenshinRegistries;
import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.entry.RegistryEntry.Reference;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Util;

public final class ElementComponentImpl implements ElementComponent {
	protected static final Set<Class<LivingEntity>> DENIED_ENTITIES = new HashSet<>();
	private final LivingEntity owner;
	private final Map<Element, ElementHolder> elementHolders = new ConcurrentHashMap<>();
	private final Logger LOGGER = OriginsGenshin.sublogger(ElementComponent.class);
	private int triggeredReactionsAtAge = -1;
	private int electroChargedCooldown = -1;
	private @Nullable LivingEntity electroChargedOrigin = null;
	private int burningCooldown = -1;
	private @Nullable LivingEntity burningOrigin = null;

	public ElementComponentImpl(LivingEntity owner) {
		this.owner = owner;

		for (final Element element : Element.values()) elementHolders.put(element, ElementHolder.of(owner, element));
	}

	@Override
	public boolean isElectroChargedOnCD() {
		return this.owner.age < this.electroChargedCooldown;
	}
	
	@Override
	public boolean isBurningOnCD() {
		return this.owner.age < this.burningCooldown;
	}

	@Override
	public void resetElectroChargedCD() {
		this.electroChargedCooldown = this.owner.age + 20;
	}

	@Override
	public void resetBurningCD() {
		this.burningCooldown = this.owner.age + 5;
	}
	
	@Override
	public void setElectroChargedOrigin(@Nullable LivingEntity origin) {
		this.electroChargedOrigin = origin;
	}

	@Override
	public void setBurningOrigin(@Nullable LivingEntity origin) {
		this.burningOrigin = origin;
	}

	@Override
	public @Nullable LivingEntity getElectroChargedOrigin() {
		return this.electroChargedOrigin;
	}

	public @Nullable LivingEntity getBurningOrigin() {
		return this.burningOrigin;
	}

	@Override
	public LivingEntity getOwner() {
		return this.owner;
	}

	@Override
	public ElementHolder getElementHolder(Element element) {
		return this.elementHolders.computeIfAbsent(element, e -> ElementHolder.of(owner, e));
	}
	
	@Override
	public boolean canApplyElement(Element element, InternalCooldownContext icdContext, boolean handleICD) {
		if (element.bypassesInternalCooldown() || !icdContext.hasOrigin()) return true;

		return this.getElementHolder(element).canApplyElement(element, icdContext, true)
			&& !ElementComponentImpl.DENIED_ENTITIES.stream().anyMatch(c -> c.isInstance(owner));
	}

	@Override
	public List<ElementalReaction> addElementalApplication(ElementalApplication application, InternalCooldownContext icdContext) {
		// Only do this on the server.
		if (application.getEntity().getWorld().isClient) return Collections.emptyList();

		if (application.isAuraElement() && application.isGaugeUnits() && this.getAppliedElements().length() > 0)
			application = application.asNonAura();

		// The elemental application is empty.
		if (application.isEmpty()) return Collections.emptyList();

		// The Element is still in ICD.
		if (!this.canApplyElement(application.getElement(), icdContext, true)) return new ArrayList<>();

		// Element has been reapplied, no reactions are triggered.
		if (this.attemptReapply(application)) return Collections.emptyList();

		final Set<ElementalReaction> triggeredReactions = this.triggerReactions(application, icdContext.getOrigin());
		
		LOGGER.info("Current element data: {}", getElementHolder(application.getElement()).getElementalApplication());
		LOGGER.info("Currently applied elements: {}", this.getAppliedElements());

		ElementComponent.sync(owner);

		return new ArrayList<>(triggeredReactions);
	}

	/*
	@Override
	public boolean hasElementalApplication(Element element) {
		return elementHolder
			.values().stream()
			.map(ElementHolder::getElementalApplication)
			.anyMatch(application -> application != null && application.isOfElement(element) && !application.isEmpty());
	}
	*/

	@Override
	public Array<ElementalApplication> getAppliedElements() {
		return new Array<>(
			elementHolders
				.values().stream()
				.map(ElementHolder::getElementalApplication)
				.filter(application -> application != null && !application.isEmpty())
		);
	}

	@Override
	public List<ElementalReaction> applyFromDamageSource(ElementalDamageSource source) {
		return addElementalApplication(source.getElementalApplication(), source.getIcdContext());
	};

	@Override
	public void writeToNbt(@Nonnull NbtCompound tag) {
		final NbtList list = new NbtList();

		this.getAppliedElements()
			.forEach(application -> list.add(application.asNbt()));

		tag.put("AppliedElements", list);
		tag.putLong("SyncedAt", owner.getWorld().getTime());
		tag.putInt("ElectroChargedCooldown", electroChargedCooldown);
		tag.putInt("BurningCooldown", burningCooldown);

		// LOGGER.info("Wrote NBT for {} at: {} ({})", owner, owner.getWorld().getTime(), Util.getMeasuringTimeMs());
	}

	@Override
	public void readFromNbt(@Nonnull NbtCompound tag) {
		this.electroChargedCooldown = tag.getInt("ElectroChargedCooldown");
		this.burningCooldown = tag.getInt("BurningCooldown");

		final NbtList list = tag.getList("AppliedElements", NbtElement.COMPOUND_TYPE);
		final long syncedAt = tag.getLong("SyncedAt");
		
		// LOGGER.info("Read NBT for {} at: {} ({}) | Synced at server time: {}", owner, owner.getWorld().getTime(), Util.getMeasuringTimeMs(), syncedAt);
		// LOGGER.info("Current NbtList: {}", list);

		this.elementHolders
			.values().stream()
			.forEach(holder -> holder.setElementalApplication(null));

		for (final NbtElement nbt : list) {
			if (!(nbt instanceof final NbtCompound compound)) return;

			final ElementalApplication application = ElementalApplications.fromNbt(owner, compound, syncedAt);

			this.getElementHolder(application.getElement())
				.setElementalApplication(application);

			if (owner.getWorld() instanceof ServerWorld) LOGGER.info("[{}] Adding ElementalApplication: {}", owner.getWorld().getClass().getSimpleName(), application);
		}
 	}

	@Override
	public void tick() {
		ElectroChargedElementalReaction.mixin$tick(this.owner);
		AbstractBurningElementalReaction.mixin$tick(this.owner);

		final int tickedElements = this
			.getAppliedElements()
			.peek(application -> application.tick())
			.length();

		if (tickedElements > 0) this.removeConsumedElements();
	}

	private void removeConsumedElements() {
		final boolean hasRemovedElements = elementHolders
			.values().stream()
			.filter(ElementHolder::hasElementalApplication)
			.filter(ec -> ec.getElementalApplication().isEmpty())
			.peek(ec -> ec.setElementalApplication(null))
			.count() > 0;
		
		if (hasRemovedElements) ElementComponent.sync(owner);
	}

	@Override
	public Optional<Integer> getHighestElementPriority() {
		return this
			.getAppliedElements()
			.sortElements((a, b) -> a.getElement().getPriority() - b.getElement().getPriority())
			.findFirst()
			.map(application -> application.getElement().getPriority());
	}

	public Array<ElementalApplication> getPrioritizedElements() {
		final Optional<Integer> priority = this.getHighestElementPriority();

		return priority.isPresent()
			? new Array<>(
				elementHolders
					.values().stream()
					.map(ElementHolder::getElementalApplication)
					.filter(application -> {
						LOGGER.info("Element: {}, shouldBeRemoved: {}, elementPriority: {}, priority: {}", application.getElement(), application.isEmpty(), application.getElement().getPriority(), priority.get());

						return !application.isEmpty() && application.getElement().getPriority() == priority.get();
					})
			)
			: new Array<>();
	}

	private Stream<ElementalReaction> getTriggerableReactions(int priority, ElementalApplication triggeringElement) {
		final Array<Element> validElements = this.getAppliedElements()
			.filter(application -> application.getElement().getPriority() == priority)
			.map(ElementalApplication::getElement);

		// LOGGER.info("Valid elements: {} | Triggering element: {}", validElements, triggeringElement);

		return OriginsGenshinRegistries.ELEMENTAL_REACTION
			.streamEntries()
			.map(Reference::value)
//			.peek(r -> LOGGER.info("(InStream) Reaction: {} | isTriggerable: {} | hasAnyElement: {} | highestElementPriority: {} ({})", r.getId(), r.isTriggerable(owner), r.hasAnyElement(validElements), r.getHighestElementPriority(), r.getHighestElementPriority() == priority))
			.filter(reaction -> reaction.isTriggerable(owner) && reaction.hasAnyElement(validElements) && reaction.getHighestElementPriority() == priority)
			.sorted(Comparator.comparing(reaction -> reaction.getPriority(triggeringElement)));
//			.peek(r -> LOGGER.info("(InStream/Ordered) Reaction: {} | Priority: {}", r.getId(), r.getPriority(triggeringElement)));
	}

	/**
	 * Attempts to reapply an {@link ElementalApplication Elemental Application}. <br> <br>
	 * 
	 * This method returns whether the provided Elemental Application was "reapplied" in some way,
	 * where {@code true} means that the element has been "reapplied" and cannot be used in an
	 * Elemental Reaction and {@code false} means that the element has not been "reapplied" and can
	 * be used in an Elemental Reaction. <br> <br>
	 * 
	 * This method also does <b>not</b> guarantee that all Elemental Applications provided are 
	 * indeed reapplied to their respective Elements, as they can be discarded due to the current
	 * Element priority.
	 * 
	 * @param application The {@code ElementalApplication} to reapply.
	 * 
	 * @return {@code true} if the Elemental Application was "reapplied", {@code false} otherwise.
	 */
	private boolean attemptReapply(ElementalApplication application) {
		final ElementalApplication currentApplication = this.getElementalApplication(application.getElement());

		// LOGGER.info("Current application: {} | Application: {} | CanBeAura: {}", currentApplication, application, application.getElement().canBeAura());

		if (currentApplication != null && !currentApplication.isEmpty() && application.getElement().canBeAura()) {
			Optional<Integer> priority = this.getHighestElementPriority();

			if (!priority.isPresent() || priority.get() == currentApplication.getElement().getPriority()) {
				currentApplication.reapply(application);
			} else {
				AbstractBurningElementalReaction.mixin$forceReapplyDendroWhenBurning(this, application);
			}

			return true;
		} else return false;
	}

	/**
	 * Triggers all possible Elemental Reactions.
	 * @param application The {@link ElementalApplication} to apply to this entity.
	 * @param origin The origin of the {@link ElementalApplication}.
	 */
	private Set<ElementalReaction> triggerReactions(ElementalApplication application, @Nullable LivingEntity origin) {
		/**
		 * Get the current element priority to keep track. 
		 * 
		 * For instance, if an element of priority 1 (p1) is added, and the current priority before
		 * that was 2, Element p1 SHOULD, in theory, "lock" the higher gauges from being reacted
		 * with.
		 */
		final Optional<Integer> optionalPriority = this.getHighestElementPriority();
		final ElementHolder context = this.getElementHolder(application.getElement());

		LOGGER.info("Triggered reactions at age: {}, Current age: {}, Current Priority: {}, Applied elements:", triggeredReactionsAtAge, this.owner.age, optionalPriority.orElse(-1));
		
		context.setElementalApplication(application);

		for (ElementalApplication element : this.getAppliedElements()) 
			LOGGER.info("\t- {}", element.getElement());

		// At least one element must be applied for a priority to exist; no priority, no applied element.
		if (!optionalPriority.isPresent()) {
			if (!context.getElement().canBeAura()) context.setElementalApplication(null);

			return Collections.emptySet();
		}

		int priority = Math.min(optionalPriority.get(), application.getElement().getPriority());

		Optional<ElementalReaction> optional = this
			.getTriggerableReactions(priority, application)
			.findFirst();

		optional = AbstractBurningElementalReaction.mixin$changeReaction(optional, this, application);

		boolean applyElementAsAura = true;
		final Set<ElementalReaction> triggeredReactions = new HashSet<>();

		LOGGER.info("Target elemental priority: {}", priority);
		LOGGER.info("Set triggered reactions at age to: {}", this.owner.age);
		triggeredReactionsAtAge = this.owner.age;

		while (optional.isPresent() && (application.getCurrentGauge() > 0 || optional.get().isTriggerable(owner))) {
			final ElementalReaction reaction = optional.get();

			LOGGER.info("Triggering reaction: {}, Current Gauge ({}): {}", reaction.getId(), application.getElement(), application.getCurrentGauge());

			reaction.trigger(owner, origin);
			applyElementAsAura = applyElementAsAura && reaction.shouldApplyResultAsAura();

			triggeredReactions.add(reaction);

			if (reaction.shouldEndReactionTrigger()) break;

			optional = this
				.getTriggerableReactions(priority, application)
				.filter(r -> AbstractBurningElementalReaction.mixin$onlyAllowPyroReactions(!triggeredReactions.stream().anyMatch(r2 -> r2.idEquals(r)), this, r))
				.findFirst();

			if (optional.isEmpty() && !reaction.shouldPreventPriorityUpgrade()) {
				final int newPriority = this.getHighestElementPriority().orElse(-1);

				if (newPriority == -1 || newPriority >= priority) break;

				LOGGER.info("Target elemental priority has been upgraded: {} -> {}", priority, newPriority);

				priority = newPriority;
				optional = this
					.getTriggerableReactions(priority, application)
					.filter(r -> AbstractBurningElementalReaction.mixin$onlyAllowPyroReactions(!triggeredReactions.stream().anyMatch(r2 -> r2.idEquals(r)), this, r))
					.findFirst();

				if (optional.isPresent()) 
					LOGGER.info("Found reaction after priority upgrade: {}, Current Gauge ({}): {}", optional.get().getId(), application.getElement(), application.getCurrentGauge());
			}
		}
		
		LOGGER.info("No more reactions may be triggered! | Current Gauge ({}): {}", application.getElement(), application.getCurrentGauge());
		LOGGER.info("Element: {} | CanBeAura: {} | Triggered reactions: {} | Apply Element as Aura: {} | Is Gauge Units: {}", context.getElement(), context.getElement().canBeAura(), triggeredReactions.size(), applyElementAsAura, application.isGaugeUnits());

		if (!context.getElement().canBeAura() || (triggeredReactions.size() > 0 && !applyElementAsAura && application.isGaugeUnits()) || AbstractBurningElementalReaction.mixin$allowDendroPassthrough(this.getHighestElementPriority().orElse(Integer.MIN_VALUE) < application.getElement().getPriority(), this, application)) {
			LOGGER.info("Removing application: {}", application);

			context.setElementalApplication(null);
		} else if (application.isGaugeUnits()) {
			LOGGER.info("Setting as aura: {}", application);

			context.setElementalApplication(application.asAura());
		}

		return triggeredReactions;
	}
}
