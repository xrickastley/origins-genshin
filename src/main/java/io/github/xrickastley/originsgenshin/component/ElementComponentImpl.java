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

public class ElementComponentImpl implements ElementComponent {
	private final LivingEntity owner;
	private final ArrayList<ElementalApplication> appliedElements = new ArrayList<>();
	/**
	 * Structure:
	 * Element -> ConcurrentHashMap (stores sourceTags and their respective ICD data)
	 * 					-> Pair<Integer, Integer> (lastApplied, 3-hit rule)
	 */
	private final ConcurrentHashMap<Element, ConcurrentHashMap<String, InternalCooldownData>> internalCooldowns = new ConcurrentHashMap<>();

	public ElementComponentImpl(LivingEntity owner) {
		this.owner = owner;
	}

	@Override
	public boolean canApplyElement(Element element, String sourceTag) {
		return this.canApplyElement(element, sourceTag, false);
	}
	
	@Override
	public boolean canApplyElement(Element element, String sourceTag, boolean handleICD) {
		final Logger LOGGER = OriginsGenshin.sublogger(ElementComponent.class);
		
		LOGGER.info("({}) Element#bypassesInternalCooldown(): {}", element.toString(), element.bypassesInternalCooldown());

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
	public @Nullable ElementalReaction addElementalApplication(ElementalApplication application, String sourceTag, @Nullable LivingEntity origin) {
		return application.isUsingGaugeUnits()
			? this.addElementalApplication(application.getElement(), sourceTag, application.getGaugeUnits(), origin)
			: this.addElementalApplication(application.getElement(), sourceTag, application.getGaugeUnits(), application.getDuration(), origin);
	}

	@Override
	public @Nullable ElementalReaction addElementalApplication(final Element element, String sourceTag, double gaugeUnits, @Nullable LivingEntity origin) {
		// The Element is still in ICD.
		if (!canApplyElement(element, sourceTag, true)) return null; 

		if (gaugeUnits <= 0) return null;

		// Check if the Element has already been applied.
		final Stream<ElementalApplication> applications = new ArrayList<>(appliedElements)
			.stream()
			.filter(application -> application.isOfElement(element))
			.map(application -> {
				application.reapply(element, gaugeUnits);

				return application;
			});

		if (applications.count() > 0) return null;

		// Check if a reaction can be triggered first.
		final ElementalApplication application = ElementalApplication.usingGaugeUnits(owner, element, gaugeUnits, false);

		// Apply the initial ElementalApplication first, as this allows Reactions to include it.
		appliedElements.add(application);

		final Optional<Reference<ElementalReaction>> reaction = OriginsGenshinRegistries.ELEMENTAL_REACTION
			.streamEntries()
			.filter(ref -> ref.value().isTriggerable(owner))
			.sorted(Comparator.comparing(ref -> ref.value().getPriority(owner)))
			.findFirst();

		// If an Elemental Reaction can be triggered
		if (reaction.isPresent()) {
			// Trigger said reaction.
			reaction.get().value().trigger(owner);
		} else {
			// Otherwise, our applied element is considered an Aura Element
			// Remove the application.
			appliedElements.remove(application);
			
			// Only create Aura Elements out of Elements that allow it.
			if (element.canBeAura()) appliedElements.add(
				ElementalApplication.usingGaugeUnits(owner, element, gaugeUnits, true)
			);
		}

		ElementComponent.sync(owner);

		return reaction
			.map(ref -> ref.value())
			.orElse(null);
	}	

	@Override
	public @Nullable ElementalReaction addElementalApplication(final Element element, String sourceTag, double gaugeUnits, double duration, @Nullable LivingEntity origin) {
		// The Element is still in ICD.
		if (!canApplyElement(element, sourceTag, true)) return null;

		if (gaugeUnits <= 0) return null;

		// TODO: Priority system.

		final ElementalApplication application = ElementalApplication.usingDuration(owner, element, gaugeUnits, duration);

		// Check if the Element has already been applied.
		final Stream<ElementalApplication> applications = new ArrayList<>(appliedElements)
			.stream()
			.filter(application2 -> application2.isOfElement(element))
			.map(application2 -> {
				application2.reapply(application);

				return application2;
			});

		if (applications.count() > 0) return null;

		// Check if a reaction can be triggered first.
		// Apply the initial ElementalApplication first, as this allows Reactions to include it.
		appliedElements.add(application);

		final Optional<Reference<ElementalReaction>> reaction = OriginsGenshinRegistries.ELEMENTAL_REACTION
			.streamEntries()
			.filter(ref -> ref.value().isTriggerable(owner))
			.sorted(Comparator.comparing(ref -> ref.value().getPriority(owner)))
			.findFirst();

		// If an Elemental Reaction can be triggered
		if (reaction.isPresent()) {
			// Trigger said reaction.
			reaction.get().value().trigger(owner);
		}

		appliedElements.stream().forEach(System.out::println);

		ElementComponent.sync(owner);

		return reaction
			.map(ref -> ref.value())
			.orElse(null);
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
			.filter(application -> application.getCurrentGauge() > 0);
	}

	@Override
	public @Nullable ElementalReaction applyFromDamageSource(ElementalDamageSource source) {
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
		new ArrayList<>(appliedElements)
			.stream()
			.filter(ElementalApplication::shouldBeRemoved)
			.forEach(application -> appliedElements.remove(application));
		
		ElementComponent.sync(owner);
	}
}
