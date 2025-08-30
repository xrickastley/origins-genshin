package io.github.xrickastley.originsgenshin.component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

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
import io.github.xrickastley.originsgenshin.element.reaction.QuickenElementalReaction;
import io.github.xrickastley.originsgenshin.factory.OriginsGenshinGameRules;
import io.github.xrickastley.originsgenshin.registry.OriginsGenshinRegistries;
import io.github.xrickastley.originsgenshin.util.Array;
import io.github.xrickastley.originsgenshin.util.ImmutablePair;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.entry.RegistryEntry.Reference;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;

import javax.annotation.Nonnull;

public final class ElementComponentImpl implements ElementComponent {
	protected static final Set<Class<LivingEntity>> DENIED_ENTITIES = new HashSet<>();
	private final LivingEntity owner;
	private final Map<Element, ElementHolder> elementHolders = new ConcurrentHashMap<>();
	private final Logger LOGGER = OriginsGenshin.sublogger(ElementComponent.class);
	private Pair<ElementalReaction, Long> lastReaction = new Pair<>(null, -1L);
	private long electroChargedCooldown = -1;
	private @Nullable LivingEntity electroChargedOrigin = null;
	private long burningCooldown = -1;
	private @Nullable LivingEntity burningOrigin = null;
	private CrystallizeShield crystallizeShield = null;
	private int crystallizeShieldReducedAt = -1;

	public ElementComponentImpl(LivingEntity owner) {
		this.owner = owner;

		for (final Element element : Element.values()) elementHolders.put(element, ElementHolder.of(owner, element));
	}

	@Override
	public boolean isElectroChargedOnCD() {
		return this.owner.getWorld().getTime() < this.electroChargedCooldown;
	}

	@Override
	public boolean isBurningOnCD() {
		return this.owner.getWorld().getTime() < this.burningCooldown;
	}

	@Override
	public void resetElectroChargedCD() {
		this.electroChargedCooldown = this.owner.getWorld().getTime() + 20;
	}

	@Override
	public void resetBurningCD() {
		this.burningCooldown = this.owner.getWorld().getTime() + 5;
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
	public void setCrystallizeShield(Element element, double amount) {
		this.crystallizeShield = new CrystallizeShield(element, amount, this.owner.getWorld().getTime());

		ElementComponent.sync(owner);
	}

	@Override
	public @Nullable Pair<Element, Double> getCrystallizeShield() {
		return this.crystallizeShield == null
			? null
			: new Pair<Element,Double>(this.crystallizeShield.element, this.crystallizeShield.amount);
	}

	@Override
	public float reduceCrystallizeShield(DamageSource source, float amount) {
		if (!(source instanceof final ElementalDamageSource eds) || this.crystallizeShield == null) return 0;

		final float reduced = this.crystallizeShield.reduce(eds, amount);

		if (reduced > 0) this.crystallizeShieldReducedAt = this.owner.age;

		return reduced;
	}

	@Override
	public boolean reducedCrystallizeShield() {
		return this.crystallizeShieldReducedAt == this.owner.age;
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
	public Pair<ElementalReaction, Long> getLastReaction() {
		return ImmutablePair.of(this.lastReaction);
	}

	// TO BE USED ONLY INTERNALLY.
	public void setLastReaction(Pair<ElementalReaction, Long> lastReaction) {
		this.lastReaction = lastReaction;
	}

	@Override
	public boolean canApplyElement(Element element, InternalCooldownContext icdContext, boolean handleICD) {
		if (element.bypassesInternalCooldown() || !icdContext.hasOrigin()) return true;

		return this.getElementHolder(element).canApplyElement(element, icdContext, true)
			&& !ElementComponentImpl.DENIED_ENTITIES.stream().anyMatch(c -> c.isInstance(owner));
	}

	@Override
	public List<ElementalReaction> addElementalApplication(ElementalApplication application, InternalCooldownContext icdContext) {
		// Only do this on the server || Only do this when doElements is true.
		if (application.getEntity().getWorld().isClient || !application.getEntity().getWorld().getGameRules().getBoolean(OriginsGenshinGameRules.DO_ELEMENTS)) return Collections.emptyList();

		if (application.isAuraElement() && application.isGaugeUnits() && this.getAppliedElements().length() > 0)
			application = application.asNonAura();

		// The elemental application is empty.
		if (application.isEmpty()) return Collections.emptyList();

		// The Element is still in ICD.
		if (!this.canApplyElement(application.getElement(), icdContext, true)) return new ArrayList<>();

		// Element has been reapplied, no reactions are triggered.
		if (this.attemptReapply(application)) return Collections.emptyList();

		final Set<ElementalReaction> triggeredReactions = this.triggerReactions(application, icdContext.getOrigin());

		LOGGER.debug("Current element data: {}", getElementHolder(application.getElement()).getElementalApplication());
		LOGGER.debug("Currently applied elements: {}", this.getAppliedElements());

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
		tag.putLong("ElectroChargedCooldown", electroChargedCooldown);
		tag.putLong("BurningCooldown", burningCooldown);

		if (this.lastReaction.getLeft() != null) {
			final NbtCompound lastReaction = new NbtCompound();

			lastReaction.putString("Id", this.lastReaction.getLeft().getId().toString());
			lastReaction.putLong("Time", this.lastReaction.getRight());

			tag.put("LastReaction", lastReaction);
		}

		if (this.crystallizeShield != null && !this.crystallizeShield.isEmpty())
			crystallizeShield.writeToNbt(tag);
	}

	@Override
	public void readFromNbt(@Nonnull NbtCompound tag) {
		this.electroChargedCooldown = tag.getLong("ElectroChargedCooldown");
		this.burningCooldown = tag.getLong("BurningCooldown");

		if (tag.contains("LastReaction")) {
			final NbtCompound lastReaction = tag.getCompound("LastReaction");

			this.lastReaction = new Pair<>(
				OriginsGenshinRegistries.ELEMENTAL_REACTION.get(Identifier.tryParse(lastReaction.getString("Id"))),
				lastReaction.getLong("Time")
			);
		}

		this.crystallizeShield = tag.contains("CrystallizeShield")
			? CrystallizeShield.ofNbt(tag.getCompound("CrystallizeShield"))
			: null;

		final NbtList list = tag.getList("AppliedElements", NbtElement.COMPOUND_TYPE);
		final long syncedAt = tag.getLong("SyncedAt");

		this.elementHolders
			.values().stream()
			.forEach(holder -> holder.setElementalApplication(null));

		for (final NbtElement nbt : list) {
			if (!(nbt instanceof final NbtCompound compound)) return;

			final ElementalApplication application = ElementalApplications.fromNbt(owner, compound, syncedAt);

			this.getElementHolder(application.getElement())
				.setElementalApplication(application);

			if (owner.getWorld() instanceof ServerWorld) LOGGER.debug("[{}] Adding ElementalApplication: {}", owner.getWorld().getClass().getSimpleName(), application);
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

		if (this.crystallizeShield != null) crystallizeShield.tick(this);
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
					.filter(application -> !application.isEmpty() && application.getElement().getPriority() == priority.get())
			)
			: new Array<>();
	}

	private Stream<ElementalReaction> getTriggerableReactions(int priority, ElementalApplication triggeringElement) {
		final Array<Element> validElements = this.getAppliedElements()
			.filter(application -> application.getElement().getPriority() == priority)
			.map(ElementalApplication::getElement);

		return OriginsGenshinRegistries.ELEMENTAL_REACTION
			.streamEntries()
			.map(Reference::value)
			.filter(reaction -> reaction.isTriggerable(owner) && reaction.hasAnyElement(validElements) && reaction.getHighestElementPriority() == priority)
			.sorted(Comparator.comparing(reaction -> reaction.getPriority(triggeringElement)));
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
	 * Element priority. <br> <br>
	 *
	 * Elements will <b>only</b> be reapplied if the Element in question has the same priority as
	 * the current "highest element priority".
	 *
	 * @param application The {@code ElementalApplication} to reapply.
	 *
	 * @return {@code true} if the Elemental Application was "reapplied", {@code false} otherwise.
	 */
	private boolean attemptReapply(ElementalApplication application) {
		final ElementalApplication currentApplication = this.getElementalApplication(application.getElement());

		if (QuickenElementalReaction.mixin$preventReapplication(application, this)) return false;

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
	 * Triggers all possible Elemental Reactions. <br> <br>
	 *
	 * Elemental Reactions adhere to the rules of Element priority, where only triggerable
	 * reactions containing elements with the highest priority are considered. <br> <br>
	 *
	 * <h3>Triggering a Reaction</h3>
	 *
	 * When an element is already applied to this entity and another element is applied, an
	 * Elemental Reaction may be triggered. <br> <br>
	 *
	 * Each registered Elemental Reaction is filtered based on the priorities of the elements
	 * participating in that reaction, where reactions only containing elements with the same
	 * priority are considered. <br> <br>
	 *
	 * After that, the candidate reactions are once again sorted based on the currently applied
	 * element's priority. The reaction with the highest priority for the currently applied
	 * element will be the triggered reaction.
	 *
	 * <h3>Triggering Multiple Reactions</h3>
	 *
	 * After a reaction is triggered, so long as the currently applied element still contains
	 * leftover Gauge Units, an attempt to find another reaction that can be triggered is made.
	 * If a reaction is found, it is then triggered and the cycle repeats again. <br> <br>
	 *
	 * <h3>Priority Upgrade</h3>
	 *
	 * If no reactions can be triggered, an attempt to upgrade the priority is made first, so
	 * long as the previous reaction allows it, where the newer priority must be greater than the
	 * previous one. <br> <br>
	 *
	 * Once the priority upgrade succeeds, an attempt is made again to find a triggerable reaction.
	 * If a reaction is found, it is then triggered and the cycle repeats again with the higher
	 * priority. Otherwise, no more attempts are made to trigger reactions afterwards.
	 *
	 * <h3>Applying as an Aura Element</h3>
	 *
	 * Normally, the triggering element is removed when at least one reaction has been triggered.
	 * However, the triggering element can be applied as an aura element afterwards if all
	 * participating reactions have {@link ElementalReaction#shouldApplyResultAsAura() ElementalReaction#shouldApplyResultAsAura}
	 * enabled. <br> <br>
	 *
	 * Do note that only {@code GAUGE_UNIT} Elemental Applications are subject to removal.
	 * {@code DURATION} Elemental Applications are not removed or accounted for by this method.
	 *
	 * @param application The {@link ElementalApplication} to apply to this entity.
	 * @param origin The origin of the {@link ElementalApplication}.
	 */
	private Set<ElementalReaction> triggerReactions(ElementalApplication application, @Nullable LivingEntity origin) {
		final Optional<Integer> optionalPriority = this.getHighestElementPriority();
		final ElementHolder context = this.getElementHolder(application.getElement());

		LOGGER.debug("Triggered reactions at age: {}, Current age: {}, Current Priority: {}, Applied elements: {}", lastReaction, this.owner.age, optionalPriority.orElse(-1), this.getAppliedElements());

		context.setElementalApplication(application);

		// At least one element must be applied for a priority to exist; no priority, no applied element.
		if (!optionalPriority.isPresent()) {
			if (!context.getElement().canBeAura()) context.setElementalApplication(null);

			return Collections.emptySet();
		}

		int priority = Math.min(optionalPriority.get(), application.getElement().getPriority());

		Optional<ElementalReaction> optional = this
			.getTriggerableReactions(priority, application)
			.filter(reaction -> reaction.hasElement(application.getElement()))
			.findFirst();

		optional = AbstractBurningElementalReaction.mixin$changeReaction(optional, this, application);

		boolean applyElementAsAura = true;
		final Set<ElementalReaction> triggeredReactions = new LinkedHashSet<>();

		LOGGER.debug("Target elemental priority: {}", priority);
		LOGGER.debug("Set triggered reactions at age to: {}", this.owner.age);

		while (optional.isPresent() && (application.getCurrentGauge() > 0 || optional.get().isTriggerable(owner))) {
			final ElementalReaction reaction = optional.get();

			LOGGER.debug("Triggering reaction: {}, Current Gauge ({}): {}", reaction.getId(), application.getElement(), application.getCurrentGauge());

			reaction.trigger(owner, origin);
			applyElementAsAura = applyElementAsAura && reaction.shouldApplyResultAsAura();

			triggeredReactions.add(reaction);

			if (reaction.shouldEndReactionTrigger()) break;

			optional = this
				.getTriggerableReactions(priority, application)
				.filter(r -> AbstractBurningElementalReaction.mixin$onlyAllowPyroReactions(!triggeredReactions.stream().anyMatch(r2 -> r2.idEquals(r)), this, r))
				.filter(Predicate.not(reaction::preventsReaction))
				.findFirst();

			if (optional.isEmpty() && !reaction.shouldPreventPriorityUpgrade()) {
				final int newPriority = this.getHighestElementPriority().orElse(-1);

				if (newPriority == -1 || newPriority >= priority) break;

				LOGGER.debug("Target elemental priority has been upgraded: {} -> {}", priority, newPriority);

				priority = newPriority;
				optional = this
					.getTriggerableReactions(priority, application)
					.filter(r -> AbstractBurningElementalReaction.mixin$onlyAllowPyroReactions(!triggeredReactions.stream().anyMatch(r2 -> r2.idEquals(r)), this, r))
					.findFirst();

				if (optional.isPresent())
					LOGGER.debug("Found reaction after priority upgrade: {}, Current Gauge ({}): {}", optional.get().getId(), application.getElement(), application.getCurrentGauge());
			}
		}

		final Optional<ElementalReaction> firstReaction = triggeredReactions.stream().findFirst();

		if (firstReaction.isPresent())
			this.lastReaction = new Pair<>(firstReaction.get(), this.owner.getWorld().getTime());

		LOGGER.debug("No more reactions may be triggered! | Current Gauge ({}): {}", application.getElement(), application.getCurrentGauge());
		LOGGER.debug("Element: {} | CanBeAura: {} | Triggered reactions: {} | Apply Element as Aura: {} | Is Gauge Units: {}", context.getElement(), context.getElement().canBeAura(), triggeredReactions.size(), applyElementAsAura, application.isGaugeUnits());

		if (!context.getElement().canBeAura() || (triggeredReactions.size() > 0 && !applyElementAsAura && application.isGaugeUnits()) || AbstractBurningElementalReaction.mixin$allowDendroPassthrough(this.getHighestElementPriority().orElse(Integer.MIN_VALUE) < application.getElement().getPriority(), this, application)) {
			LOGGER.debug("Removing application: {}", application);

			context.setElementalApplication(null);
		} else if (application.isGaugeUnits()) {
			LOGGER.debug("Setting as aura: {}", application);

			context.setElementalApplication(application.asAura());
		}

		return triggeredReactions;
	}

	private static class CrystallizeShield {
		private final Element element;
		private final long appliedAt;
		private double amount;

		private CrystallizeShield(final Element element, final double amount, final long appliedAt) {
			this.element = element;
			this.appliedAt = appliedAt;
			this.amount = amount;
		}

		private static CrystallizeShield ofNbt(final NbtCompound tag) {
			return new CrystallizeShield(Element.valueOf(tag.getString("Element")), tag.getDouble("Amount"), tag.getLong("AppliedAt"));
		}

		private float reduce(ElementalDamageSource source, float amount) {
			final double elementBonus = this.element == Element.GEO
				? 1.5 // 150% "effectiveness"
				: source.getElementalApplication().getElement() == this.element
					? 2.5 // 250% "effectiveness"
					: 1; // No "effectiveness"

			final double dmgTakenByShield = Math.min(this.amount * elementBonus, amount);
			System.out.println("Shield amount: " + this.amount);
			System.out.println("DMG Taken by Shield: " + dmgTakenByShield / elementBonus);
			System.out.println("DMG Reduced by Shield: " + dmgTakenByShield);
			// Use Math.max to guarantee >= 0 in case of FP errors.
			this.amount = Math.max(this.amount - (dmgTakenByShield / elementBonus), 0);

			return (float) dmgTakenByShield;
		}

		private void writeToNbt(@Nonnull NbtCompound tag) {
			final NbtCompound crystallizeShield = new NbtCompound();

			crystallizeShield.putString("Element", this.element.toString());
			crystallizeShield.putDouble("Amount", this.amount);
			crystallizeShield.putLong("AppliedAt", this.appliedAt);

			tag.put("CrystallizeShield", crystallizeShield);
		}

		private boolean isEmpty() {
			return this.amount <= 0 || this.element == null;
		}

		private void tick(ElementComponentImpl impl) {
			// System.out.println(String.format("age: %b | !empty: %b | result: %b", this.appliedAt + 300 >= impl.owner.getWorld().getTime(), !this.isEmpty(), this.appliedAt + 300 >= impl.owner.getWorld().getTime() && !this.isEmpty()));

			if ((this.appliedAt + 300 >= impl.owner.getWorld().getTime() && !this.isEmpty()) || impl.crystallizeShield == null) return;

			impl.crystallizeShield = null;

			ElementComponent.sync(impl.owner);
		}
	}
}
