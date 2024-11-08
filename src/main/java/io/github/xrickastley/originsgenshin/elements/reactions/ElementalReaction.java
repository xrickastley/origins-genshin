package io.github.xrickastley.originsgenshin.elements.reactions;

import java.text.DecimalFormat;
import java.util.Comparator;

import org.slf4j.Logger;

import blue.endless.jankson.annotation.Nullable;
import io.github.xrickastley.originsgenshin.OriginsGenshin;
import io.github.xrickastley.originsgenshin.components.ElementComponent;
import io.github.xrickastley.originsgenshin.elements.Element;
import io.github.xrickastley.originsgenshin.elements.ElementalApplication;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;

public abstract class ElementalReaction {
	protected final String name;
	protected final Identifier id;
	protected final double reactionCoefficient;
	protected final Pair<Element, Integer> auraElement;
	protected final Pair<Element, Integer> triggeringElement;
	protected final boolean reversable;
	protected final boolean allowChildElements;
	
	protected ElementalReaction(ElementalReactionSettings settings) {
		this.name = settings.name;
		this.id = settings.id;
		this.reactionCoefficient = settings.reactionCoefficient;
		this.auraElement = settings.auraElement;
		this.triggeringElement = settings.triggeringElement;
		this.reversable = settings.reversable;
		this.allowChildElements = settings.allowChildElements;
	}

	private @Nullable ElementalApplication getPossibleChildElement(Element element, ElementComponent component) {
		return component
			.getAppliedElements()
			.filter(application -> auraElement.getLeft().isChild(application.getElement()))
			.sorted(Comparator.comparingDouble(application -> application.getElement().getPriority()))
			.findFirst()
			.orElseGet(() -> null);
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

	public Pair<Element, Integer> getElementPair(Element element) {
		return element == auraElement.getLeft()
			? auraElement
			: element == triggeringElement.getLeft()
				? triggeringElement
				: null;
	}

	/**
	 * Gets the priority of this Elemental Reaction for a specific living entity.
	 * @param entity The living entity.
	 * @return The priority of this Elemental Reaction for the specified entity.
	 */
	public int getPriority(LivingEntity entity) {
		return ElementComponent.KEY.get(entity)
			.getAppliedElements()
			.sorted(Comparator.comparing(application -> application.getElement().getPriority()))
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
	 */
	protected abstract void onReaction(LivingEntity entity, ElementalApplication auraElement, ElementalApplication triggeringElement, double reducedGauge);

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

		if (this.allowChildElements) {
			// Supply them with their respective child elements if they are null.
			if (applicationAE == null) applicationAE = getPossibleChildElement(auraElement.getLeft(), component);
			if (applicationTE == null) applicationTE = getPossibleChildElement(triggeringElement.getLeft(), component);
		}

		return reversable
			// Any of the elements can be an Aura element.
			? applicationAE != null && applicationTE != null
			// The aura element must STRICTLY be an Aura element.
			: applicationAE != null && applicationTE != null && applicationAE.isAuraElement();
	}

	public boolean trigger(LivingEntity entity) {
		if (!isTriggerable(entity)) return false;

		final ElementComponent component = ElementComponent.KEY.get(entity);
		ElementalApplication applicationAE = component.getElementalApplication(auraElement.getLeft());
		ElementalApplication applicationTE = component.getElementalApplication(triggeringElement.getLeft());

		if (applicationTE.isAuraElement()) {
			ElementalApplication a = applicationTE;
			applicationTE = applicationAE;
			applicationAE = a;
		}

		final DecimalFormat df = new DecimalFormat("0.0");
		final Logger LOGGER = OriginsGenshin.sublogger(ElementalReaction.class);
		
		LOGGER.info("Phase: BEFORE - Aura element: {} GU {}; Triggering elements: {} GU {}; Reaction coefficient: {}", df.format(applicationAE.getCurrentGauge()), applicationAE.getElement(), df.format(applicationTE.getCurrentGauge()), applicationTE.getElement(), reactionCoefficient);
		
		final double reducedGauge = applicationAE.reduceGauge(reactionCoefficient * applicationTE.getCurrentGauge());
		/**
		 * TODO: Multi-elemental aura triggers
		 * 
		 * Ex: Cryo + Dendro
		 * When a Hydro attack consumes the Cryo aura entirely and the Hydro aura still 
		 * exists (currentGauge > 0), reacts with the Dendro aura for bloom, then 
		 * disappears (even if currentGauge > 0).
		 */
		// component.reduceElementalApplication(triggeringElement.getLeft(), reducedGauge * reactionCoefficient);

		applicationTE.reduceGauge(applicationTE.getCurrentGauge());
		
		LOGGER.info("Phase: AFTER - Aura element: {} GU {}; Triggering elements: {} GU {}; Reaction coefficient: {}", df.format(applicationAE.getCurrentGauge()), applicationAE.getElement(), df.format(applicationTE.getCurrentGauge()), applicationTE.getElement(), reactionCoefficient);

		onReaction(entity, applicationAE, applicationTE, reducedGauge);

		return true;
	}
}
