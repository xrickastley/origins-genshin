package io.github.xrickastley.originsgenshin.component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.slf4j.Logger;

import io.github.xrickastley.originsgenshin.OriginsGenshin;
import io.github.xrickastley.originsgenshin.element.Element;
import io.github.xrickastley.originsgenshin.element.ElementalApplication;
import io.github.xrickastley.originsgenshin.element.ElementalDamageSource;
import io.github.xrickastley.originsgenshin.element.InternalCooldownData;
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
	private final ArrayList<ElementalApplication> appliedElements = new ArrayList<>();
	/**
	 * Structure:
	 * Element -> ConcurrentHashMap (stores sourceTags and their respective ICD data)
	 * 					-> Pair<Integer, Integer> (lastApplied, 3-hit rule)
	 */
	private final ConcurrentHashMap<Element, ConcurrentHashMap<String, InternalCooldownData>> internalCooldowns = new ConcurrentHashMap<>();
	private final Logger LOGGER = OriginsGenshin.sublogger(ElementComponent.class);

	public ElementComponentImpl(LivingEntity owner) {
		this.owner = owner;
	}

	@Override
	public boolean canApplyElement(Element element, String sourceTag) {
		return this.canApplyElement(element, sourceTag, false);
	}
	
	@Override
	public boolean canApplyElement(Element element, String sourceTag, boolean handleICD) {
		if (element.bypassesInternalCooldown()) return true;

		final ConcurrentHashMap<String, InternalCooldownData> elementICD = internalCooldowns.getOrDefault(element, new ConcurrentHashMap<>());
		final InternalCooldownData icdData = elementICD.getOrDefault(sourceTag, InternalCooldownData.blank(owner));
		
		final boolean inICD = handleICD 
			? icdData.handleInternalCooldown() 
			: icdData.inInternalCooldown();

		elementICD.put(sourceTag, icdData);
		internalCooldowns.put(element, elementICD);

		return inICD;
	}

	@Override
	public ArrayList<ElementalReaction> addElementalApplication(ElementalApplication application, String sourceTag, @Nullable LivingEntity origin) {
		return application.isUsingGaugeUnits()
			? this.addElementalApplication(application.getElement(), sourceTag, application.getGaugeUnits(), origin)
			: this.addElementalApplication(application.getElement(), sourceTag, application.getGaugeUnits(), application.getDuration(), origin);
	}

	@Override
	public ArrayList<ElementalReaction> addElementalApplication(final Element element, String sourceTag, double gaugeUnits, @Nullable LivingEntity origin) {
		if (gaugeUnits <= 0) return null;
		
		// The Element is still in ICD.
		if (!this.canApplyElement(element, sourceTag, true)) return null;
		
		// Check if a reaction can be triggered first.
		final ElementalApplication application = ElementalApplication.usingGaugeUnits(owner, element, gaugeUnits, false);
		
		// Check if the Element has already been applied.
		if (this.attemptToReapply(application)) return null;

		final ArrayList<ElementalReaction> triggeredReactions = this.triggerPossibleReactions(application, origin);

		// Our applied element is now an Aura Element
		// Remove the application.
		appliedElements.remove(application);

		boolean applyResultAsAura = triggeredReactions
			.stream()
			.allMatch(ElementalReaction::shouldApplyResultAsAura);
		
		// Only create Aura Elements out of Elements that allow it.
		if (element.canBeAura() && applyResultAsAura) appliedElements.add(
			ElementalApplication.usingGaugeUnits(owner, element, gaugeUnits, true)
		);
		
		ElementComponent.sync(owner);

		return triggeredReactions;
	}	

	@Override
	public ArrayList<ElementalReaction> addElementalApplication(final Element element, String sourceTag, double gaugeUnits, double duration, @Nullable LivingEntity origin) {
		if (gaugeUnits <= 0) return null;
		
		// The Element is still in ICD.
		if (!this.canApplyElement(element, sourceTag, true)) return null;

		final ElementalApplication application = ElementalApplication.usingDuration(owner, element, gaugeUnits, duration);

		if (this.attemptToReapply(application)) return null;

		// Check if a reaction can be triggered first.
		// Apply the initial ElementalApplication first, as this allows Reactions to include it.
		appliedElements.add(application);

		final ArrayList<ElementalReaction> triggeredReactions = this.triggerPossibleReactions(application, origin);

		ElementComponent.sync(owner);

		return triggeredReactions;
	}

	@Override
	public boolean hasElementalApplication(Element element) {
		return new ArrayList<>(appliedElements)
			.stream()
			.anyMatch(application -> application.isOfElement(element));
	}

	@Override
	public double reduceElementalApplication(Element element, double gaugeUnits) {
		return Optional.ofNullable(this.getElementalApplication(element))
			.map(application -> application.reduceGauge(gaugeUnits))
			.orElse(-1.0);
	}

	@Override
	public @Nullable ElementalApplication getElementalApplication(Element element) {
		return new ArrayList<>(appliedElements)
			.stream()
			.filter(application -> application.isOfElement(element))
			.findFirst()
			.orElseGet(() -> null);
	}

	@Override
	public Stream<ElementalApplication> getAppliedElements() {
		return new ArrayList<>(appliedElements)
			.stream()
			.filter(application -> !application.shouldBeRemoved());
	}

	@Override
	public ArrayList<ElementalReaction> applyFromDamageSource(ElementalDamageSource source) {
		return addElementalApplication(
			source.getElementalApplication(), 
			source.getSourceTag(), 
			source.getAttacker() instanceof LivingEntity origin 
				? origin
				: null
		);
	};

	@Override
	public void writeToNbt(@Nonnull NbtCompound tag) {
		final NbtList list = new NbtList();

		this.getAppliedElements()
			.forEach(application -> list.add(application.asNbt()));

		tag.put("appliedElements", list);
		tag.putInt("sentAtAge", owner.age);
	}

	@Override
	public void readFromNbt(@Nonnull NbtCompound tag) {
		final NbtList list = tag.getList("appliedElements", NbtElement.COMPOUND_TYPE);
		final int sentAtAge = tag.getInt("sentAtAge");

		// Client side: clear all applied elements and use the values provided by tag.
		appliedElements.clear();

		list.stream()
			.map(element -> ElementalApplication.fromNbt(owner, element, sentAtAge))
			.peek(application -> {
				if (owner.getWorld() instanceof ServerWorld) LOGGER.info("[{}] Adding ElementalApplication: {}", owner.getWorld().getClass().getSimpleName(), application);
			})
			.forEach(appliedElements::add);
 	}

	@Override
	public void tick() {
		new ArrayList<>(appliedElements)
			.stream()
			.forEach(application -> application.tick());

		if (appliedElements.size() > 0) removeConsumedElements();
	}

	private void removeConsumedElements() {
		// Copy to prevent ConcurrentModificationException.
		boolean hasRemovedElements = new ArrayList<>(appliedElements)
			.stream()
			.filter(ElementalApplication::shouldBeRemoved)
			.map(application -> appliedElements.remove(application))
			.anyMatch(b -> b);
		
		if (hasRemovedElements) ElementComponent.sync(owner);
	}

	@Override
	public Optional<Integer> getCurrentElementPriority() {
		return this
			.getAppliedElements()
			.sorted(Comparator.comparingDouble(application -> application.getElement().getPriority()))
			.findFirst()
			.map(application -> application.getElement().getPriority());
	}

	public Stream<ElementalApplication> getPrioritizedElements() {
		final Optional<Integer> priority = this.getCurrentElementPriority();
		
		return priority.isPresent()
			? this
				.getAppliedElements()
				.filter(application -> application.getElement().getPriority() == priority.get())
			: Stream.of();
	}

	private Stream<ElementalReaction> getTriggerableReactions() {
		final Stream<Element> validElements = this
			.getPrioritizedElements()
			.map(ElementalApplication::getElement);

		return OriginsGenshinRegistries.ELEMENTAL_REACTION
			.streamEntries()
			.map(Reference::value)
			.filter(reaction -> reaction.isTriggerable(owner) && reaction.hasAnyElement(validElements))
			.sorted(Comparator.comparing(reaction -> reaction.getPriority(owner)));
	}

	/**
	 * Attempts to reapply an {@link ElementalApplication}.
	 * @param application The {@code ElementalApplication} to reapply.
	 * @return {@code true} if the Elemental Application was reapplied, {@code false} otherwise.
	 */
	private boolean attemptToReapply(ElementalApplication application) {
		// Check if the Element has already been applied.
		final Optional<ElementalApplication> currentApplication = this
			.getPrioritizedElements()
			.filter(application2 -> application2.isOfElement(application.getElement()))
			.findFirst();

		if (currentApplication.isPresent()) {
			currentApplication
				.get()
				.reapply(application);

			return true;
		} else return false;
	}

	/**
	 * Triggers all possible Elemental Reactions.
	 * @param application The {@link ElementalApplication} to apply to this entity.
	 * @param origin The origin of the {@link ElementalApplication}.
	 */
	private ArrayList<ElementalReaction> triggerPossibleReactions(ElementalApplication application, @Nullable LivingEntity origin) {
		// Apply the initial ElementalApplication first, as this allows Reactions to check for it.
		appliedElements.add(application);

		Optional<ElementalReaction> reaction = this
			.getTriggerableReactions()
			.findFirst();

		final ArrayList<ElementalReaction> triggeredReactions = new ArrayList<>();

		while (application.getCurrentGauge() > 0 && reaction.isPresent()) {
			reaction
				.get()
				.trigger(owner, origin);
			
			triggeredReactions.add(reaction.get());

			reaction = this
				.getTriggerableReactions()
				.findFirst();
		}

		return triggeredReactions;
	}
}
