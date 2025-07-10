package io.github.xrickastley.originsgenshin.component;

import java.util.List;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.slf4j.Logger;

import io.github.xrickastley.originsgenshin.util.Array;
import io.github.xrickastley.originsgenshin.OriginsGenshin;
import io.github.xrickastley.originsgenshin.element.Element;
import io.github.xrickastley.originsgenshin.element.ElementHolder;
import io.github.xrickastley.originsgenshin.element.ElementalApplication;
import io.github.xrickastley.originsgenshin.element.ElementalDamageSource;
import io.github.xrickastley.originsgenshin.element.InternalCooldownContext;
import io.github.xrickastley.originsgenshin.element.reaction.ElementalReaction;
import io.github.xrickastley.originsgenshin.registry.OriginsGenshinRegistries;
import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.entry.RegistryEntry.Reference;
import net.minecraft.server.world.ServerWorld;

public class ElementComponentImpl implements ElementComponent {
	private final LivingEntity owner;
	private final ConcurrentHashMap<Element, ElementHolder> elementHolder = new ConcurrentHashMap<>();
	private final Logger LOGGER = OriginsGenshin.sublogger(ElementComponent.class);
	private int triggeredReactionsAtAge = -1;

	public ElementComponentImpl(LivingEntity owner) {
		this.owner = owner;

		for (final Element element : Element.values()) elementHolder.put(element, ElementHolder.of(owner, element));
	}

	@Override
	public LivingEntity getOwner() {
		return this.owner;
	}

	@Override
	public ElementHolder getElementHolder(Element element) {
		return this.elementHolder.computeIfAbsent(element, e -> ElementHolder.of(owner, e));
	}
	
	@Override
	public boolean canApplyElement(Element element, InternalCooldownContext icdContext, boolean handleICD) {
		if (element.bypassesInternalCooldown() || !icdContext.hasOrigin()) return true;

		return this.getElementHolder(element).canApplyElement(element, icdContext, true);
	}

	@Override
	public List<ElementalReaction> addElementalApplication(ElementalApplication application, InternalCooldownContext icdContext) {
		if (application.isAuraElement() && application.isGaugeUnits() && this.getAppliedElements().length() > 0)
			application = application.asNonAura();

		// The elemental application is empty.
		if (application.isEmpty()) return new ArrayList<>();

		// The Element is still in ICD.
		if (!this.canApplyElement(application.getElement(), icdContext, true)) return new ArrayList<>();

		// Element has been reapplied, no reactions are triggered.
		if (this.attemptReapply(application)) return new ArrayList<>();

		final ArrayList<ElementalReaction> triggeredReactions = this.triggerReactions(application, icdContext.getOrigin());

		// Current priority is higher, reactions have been triggered. Remove element after.
		if (this.getHighestElementPriority().orElse(-1) < application.getElement().getPriority())
			this.getElementHolder(application.getElement()).setElementalApplication(null);
		
		LOGGER.info("Current element data: {}", getElementHolder(application.getElement()).getElementalApplication());
		LOGGER.info("Currently applied elements: {}", this.getAppliedElements());

		ElementComponent.sync(owner);

		return triggeredReactions;
	}

	@Override
	public boolean hasElementalApplication(Element element) {
		return elementHolder
			.values().stream()
			.map(ElementHolder::getElementalApplication)
			.anyMatch(application -> application != null && application.isOfElement(element) && !application.isEmpty());
	}

	@Override
	public double reduceElementalApplication(Element element, double gaugeUnits) {
		return Optional.ofNullable(this.getElementalApplication(element))
			.map(application -> application.reduceGauge(gaugeUnits))
			.orElse(-1.0);
	}

	@Override
	public @Nullable ElementalApplication getElementalApplication(Element element) {
		return this
			.getElementHolder(element)
			.getElementalApplication();
	}

	@Override
	public Array<ElementalApplication> getAppliedElements() {
		return new Array<>(
			elementHolder
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
		tag.putInt("SentAtAge", owner.age);
	}

	@Override
	public void readFromNbt(@Nonnull NbtCompound tag) {
		final NbtList list = tag.getList("AppliedElements", NbtElement.COMPOUND_TYPE);
		final int sentAtAge = tag.getInt("SentAtAge");

		this.elementHolder
			.values().stream()
			.forEach(holder -> holder.setElementalApplication(null));

		LOGGER.info("Current NbtList: {}", list);

		for (final NbtElement nbt : list) {
			if (!(nbt instanceof final NbtCompound compound)) return;

			final ElementalApplication application = ElementalApplication.fromNbt(owner, compound, sentAtAge);

			if (application.isDuration()) {
				// TODO: sync by current entity age, ages are different in Render thread and Server thread.
				LOGGER.info("Application: {} | appliedAt: {} | age: {}", application, application.appliedAt, owner.age);
			}

			this.getElementHolder(application.getElement())
				.setElementalApplication(application);

			if (owner.getWorld() instanceof ServerWorld) LOGGER.info("[{}] Adding ElementalApplication: {}", owner.getWorld().getClass().getSimpleName(), application);
		}
 	}

	@Override
	public void tick() {
		final int tickedElements = this
			.getAppliedElements()
			.peek(application -> application.tick())
			.length();

		if (tickedElements > 0) removeConsumedElements();
	}

	private void removeConsumedElements() {
		// Copy to prevent ConcurrentModificationException.
		boolean hasRemovedElements = elementHolder
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
				elementHolder
					.values().stream()
					.map(ElementHolder::getElementalApplication)
					.filter(application -> {
						LOGGER.info("Element: {}, shouldBeRemoved: {}, elementPriority: {}, priority: {}", application.getElement(), application.isEmpty(), application.getElement().getPriority(), priority.get());

						return !application.isEmpty() && application.getElement().getPriority() == priority.get();
					})
			)
			: new Array<>();
	}

	private Stream<ElementalReaction> getTriggerableReactions(int priority) {
		return getTriggerableReactions(
			this.getAppliedElements()
				.filter(application -> application.getElement().getPriority() == priority)
				.map(ElementalApplication::getElement)
		);
	}

	private Stream<ElementalReaction> getTriggerableReactions(Array<Element> validElements) {
		// LOGGER.info("Valid elements:");
		// for (Element element : validElements) {
		// 	LOGGER.info("\t- {}", element);
		// };

		// LOGGER.info("Possible reactions:");
		return OriginsGenshinRegistries.ELEMENTAL_REACTION
			.streamEntries()
			.map(Reference::value)
			.filter(reaction -> {
				// LOGGER.info("\t -Reaction: {}, isTriggerable: {}, hasAnyElement: {}", reaction.getId(), reaction.isTriggerable(owner), reaction.hasAnyElement(validElements));

				// LOGGER.info("Reaction: {} | isTriggerable: {} | hasAnyElement ({}): {}", reaction.getId(), reaction.isTriggerable(owner), validElements, reaction.hasAnyElement(validElements));

				return reaction.isTriggerable(owner) && reaction.hasAnyElement(validElements);
			})
			.sorted(Comparator.comparing(reaction -> reaction.getPriority(owner)));
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

		LOGGER.info("Current application: {} | Application: {} | CanBeAura: {}", currentApplication, application, application.getElement().canBeAura());

		if (currentApplication != null && application.getElement().canBeAura()) {
			Optional<Integer> priority = this.getHighestElementPriority();

			if (!priority.isPresent() || priority.get() == currentApplication.getElement().getPriority()) {
				currentApplication.reapply(application);
			} else {
				forceReapplyDendroWhenBurning(application);
			}

			return true;
		} else return false;
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
	private void forceReapplyDendroWhenBurning(ElementalApplication application) {
		if (application.getElement() != Element.DENDRO) return;

		final Set<Element> appliedElements = this
			.getAppliedElements()
			.stream()
			.map(ElementalApplication::getElement)
			.collect(Collectors.toSet());

		if (!appliedElements.contains(Element.BURNING)) return;

		this.getElementHolder(Element.DENDRO)
			.setElementalApplication(application.asAura());
	}

	/**
	 * Triggers all possible Elemental Reactions.
	 * @param application The {@link ElementalApplication} to apply to this entity.
	 * @param origin The origin of the {@link ElementalApplication}.
	 */
	private ArrayList<ElementalReaction> triggerReactions(ElementalApplication application, @Nullable LivingEntity origin) {
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

			return new ArrayList<>();
		}

		final int priority = Math.min(optionalPriority.get(), application.getElement().getPriority());

		Optional<ElementalReaction> reaction = this
			// .getTriggerableReactions(priority)
			.getTriggerableReactions(priority)
			.findFirst();

		boolean applyElementAsAura = false;
		final ArrayList<ElementalReaction> triggeredReactions = new ArrayList<>();

		LOGGER.info("Target elemental priority: {}", priority);
		while (application.getCurrentGauge() > 0 && reaction.isPresent()) {
			System.out.println("Set triggered reactions at age to: " + this.owner.age);
			triggeredReactionsAtAge = this.owner.age;

			try {
				LOGGER.info("Triggering reaction: {}, Current Gauge ({}): {}", reaction.get().getId(), application.getElement(), application.getCurrentGauge());
			} catch (Throwable e) {
				System.out.println("catched");
				System.out.println(e);
			}

			reaction.get().trigger(owner, origin);
			applyElementAsAura = reaction.get().shouldApplyResultAsAura();

			triggeredReactions.add(reaction.get());

			reaction = this
				.getTriggerableReactions(priority)
				.filter(reaction1 -> !triggeredReactions.stream().anyMatch(reaction2 -> reaction2.idEquals(reaction1)))
				.findFirst();
		}

		LOGGER.info("Element: {} | CanBeAura: {}", context.getElement(), context.getElement().canBeAura());

		if (triggeredReactions.size() > 0 && (!applyElementAsAura || !context.getElement().canBeAura())) {
			LOGGER.info("Removing application: {}", application);

			context.setElementalApplication(null);
		}
		
		if ((applyElementAsAura || triggeredReactions.size() == 0) && application.isGaugeUnits()) {
			LOGGER.info("Setting as aura: {}", application);

			context.setElementalApplication(application.asAura());
		}

		return triggeredReactions;
	}
}
