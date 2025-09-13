package io.github.xrickastley.originsgenshin.element;

import com.mojang.serialization.Codec;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import io.github.xrickastley.originsgenshin.OriginsGenshin;
import io.github.xrickastley.originsgenshin.component.ElementComponent;
import io.github.xrickastley.originsgenshin.component.ElementComponentImpl;
import io.github.xrickastley.originsgenshin.events.ElementEvents;
import io.github.xrickastley.originsgenshin.factory.OriginsGenshinStatusEffects;
import io.github.xrickastley.originsgenshin.util.Color;
import io.github.xrickastley.originsgenshin.util.Colors;

import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;
import net.minecraft.util.dynamic.Codecs;

public enum Element {
	// Only here for Attribute Identification. Other than that, this serves no use, since Physical isn't really an Element.
	PHYSICAL(
		OriginsGenshin.identifier("physical"),
		ElementSettings
			.create()
			.setDamageColor(Color.fromRGBAHex("#ffffff"))
			.canBeAura(false)
	),
	PYRO(
		OriginsGenshin.identifier("pyro"),
		ElementSettings
			.create()
			.setTexture(OriginsGenshin.identifier("textures/element/pyro.png"))
			.setDamageColor(Colors.PYRO)
			.setPriority(2)
			.decayInheritance(false)
	),
	HYDRO(
		OriginsGenshin.identifier("hydro"),
		ElementSettings
			.create()
			.setTexture(OriginsGenshin.identifier("textures/element/hydro.png"))
			.setDamageColor(Colors.HYDRO)
			.setPriority(2)
	),
	ANEMO(
		OriginsGenshin.identifier("anemo"),
		ElementSettings
			.create()
			.setTexture(OriginsGenshin.identifier("textures/element/anemo.png"))
			.setDamageColor(Colors.ANEMO)
			.setPriority(2)
			.canBeAura(false)
	),
	ELECTRO(
		OriginsGenshin.identifier("electro"),
		ElementSettings
			.create()
			.setTexture(OriginsGenshin.identifier("textures/element/electro.png"))
			.setDamageColor(Colors.ELECTRO)
			.setPriority(2)
	),
	DENDRO(
		OriginsGenshin.identifier("dendro"),
		ElementSettings
			.create()
			.setTexture(OriginsGenshin.identifier("textures/element/dendro.png"))
			.setDamageColor(Colors.DENDRO)
			.setPriority(2)
			.setDecayRate(Decays.DENDRO_DECAY_RATE)
	),
	CRYO(
		OriginsGenshin.identifier("cryo"),
		ElementSettings
			.create()
			.setTexture(OriginsGenshin.identifier("textures/element/cryo.png"))
			.setDamageColor(Colors.CRYO)
			.setPriority(2)
	),
	GEO(
		OriginsGenshin.identifier("geo"),
		ElementSettings
			.create()
			.setTexture(OriginsGenshin.identifier("textures/element/geo.png"))
			.setDamageColor(Colors.GEO)
			.setPriority(2)
			.canBeAura(false)
	),
	FREEZE(
		OriginsGenshin.identifier("freeze"),
		ElementSettings.create()
			.setTexture(OriginsGenshin.identifier("textures/element/cryo.png"))
			.setDamageColor(Color.fromRGBAHex("#b4ffff"))
			.setPriority(2)
			.bypassesCooldown(true)
			.linkToElement(Element.CRYO)
	),
	BURNING(
		OriginsGenshin.identifier("burning"),
		ElementSettings.create()
			.setTexture(OriginsGenshin.identifier("textures/element/pyro.png"))
			.setDamageColor(Colors.PYRO)
			.setPriority(1)
			.setDecayRate(Decays.NO_DECAY_RATE)
			.bypassesCooldown(true)
			.hasAuraTax(false)
			.linkToElement(Element.PYRO)
	),
	QUICKEN(
		OriginsGenshin.identifier("quicken"),
		ElementSettings.create()
			.setTexture(OriginsGenshin.identifier("textures/element/dendro.png"))
			.setDamageColor(Color.fromRGBAHex("#01e858"))
			.setPriority(2)
			.bypassesCooldown(true)
			.linkToElement(Element.DENDRO)
			.linkGaugeDecayIf(application -> ElementComponent.KEY.get(application.getEntity()).hasElementalApplication(Element.BURNING))
	);

	public static final Codec<Element> CODEC = Codecs.NON_EMPTY_STRING.xmap(Element::valueOf, Element::toString);

	private final Identifier id;
	private final ElementSettings settings;
	private final List<Pair<Element, Predicate<ElementalApplication>>> linkedElements;

	private Element(Identifier id, ElementSettings settings) {
		this.id = id;
		this.settings = settings;
		this.linkedElements = new ArrayList<>();

		if (settings.linkedElement == null) return;

		if (settings.reverseLinkedElement) {
			this.linkedElements.add(new Pair<>(settings.linkedElement, settings.linkDecayOnlyIf));
		} else {
			settings.linkedElement.linkedElements.add(new Pair<>(this, settings.linkDecayOnlyIf));
		}
	}

	public boolean hasDecayInheritance() {
		return settings.decayInheritance;
	}

	public boolean hasTexture() {
		return settings.texture != null;
	}

	public Identifier getTexture() {
		return settings.texture;
	}

	public boolean hasDamageColor() {
		return settings.damageColor != null;
	}

	public Color getDamageColor() {
		return settings.damageColor;
	}

	public Identifier getId() {
		return this.id;
	}

	public boolean canBeAura() {
		return settings.canBeAura;
	}

	public int getPriority() {
		return this.settings.priority;
	}

	public @Nullable Function<ElementalApplication, Number> getCustomDecayRate() {
		return settings.decayRate;
	}

	public boolean bypassesInternalCooldown() {
		return this.settings.bypassesCooldown;
	}

	public boolean hasAuraTax() {
		return settings.hasAuraTax;
	}
	
	public String getString() {
		final String string = this.toString();
		final String fallback = string.substring(0, 1).toUpperCase() + string.substring(1).toLowerCase();

		return Text
			.translatableWithFallback("element.origins-genshin." + string.toLowerCase(), fallback)
			.getString();
	}

	void reduceLinkedElements(double reduction, ElementalApplication application, boolean isGaugeDecay) {
		final ElementComponent component = ElementComponent.KEY.get(application.getEntity());

		if (component == null) return;

		for (final Pair<Element, Predicate<ElementalApplication>> pair : application.getElement().linkedElements) {
			if (!component.hasElementalApplication(pair.getLeft())) continue;

			if (isGaugeDecay && !pair.getRight().test(application)) continue;

			component.getElementalApplication(pair.getLeft()).currentGauge -= reduction;
		}

		ElementComponent.sync(application.getEntity());
	}

	// These "mixins" are injected pieces of code (likening @Inject) that allow Cryo to work properly, and allow others to easily see the way it was hardcoded.
	public static void mixin$tick(ElementComponent component) {
		if (component.hasElementalApplication(Element.CRYO) && !component.getOwner().hasStatusEffect(OriginsGenshinStatusEffects.CRYO)) {
			component.getOwner().addStatusEffect(
				new StatusEffectInstance(
					OriginsGenshinStatusEffects.CRYO,
					component.getElementalApplication(Element.CRYO).getRemainingTicks(),
					0,
					true,
					false,
					true
				)
			);
		} else if (!component.hasElementalApplication(Element.CRYO) && component.getOwner().hasStatusEffect(OriginsGenshinStatusEffects.CRYO)) {
			component.getOwner().removeStatusEffect(OriginsGenshinStatusEffects.CRYO);
		}
	}

	static {
		ElementEvents.APPLIED
			.register((element, application) -> {
				if (element != Element.CRYO) return;

				application.getEntity().addStatusEffect(
					new StatusEffectInstance(
						OriginsGenshinStatusEffects.CRYO,
						application.getRemainingTicks(),
						0,
						true,
						false,
						true
					)
				);
			});

		ElementEvents.REFRESHED
			.register((element, application, _prev) -> {
				if (element != Element.CRYO || application == null) return;

				application.getEntity().removeStatusEffect(OriginsGenshinStatusEffects.CRYO);
				application.getEntity().addStatusEffect(
					new StatusEffectInstance(
						OriginsGenshinStatusEffects.CRYO,
						application.getRemainingTicks(),
						0,
						true,
						false,
						true
					)
				);
			});
	}

	/**
	 * A class used in creating data for Elements, instead of multiple overloaded constructors.
	 */
	private static class ElementSettings {
		private ElementSettings() {};

		protected Identifier texture;
		protected Color damageColor;
		protected int priority;
		protected @Nullable Function<ElementalApplication, Number> decayRate = null;
		protected boolean canBeAura = true;
		protected boolean decayInheritance = true;
		protected boolean bypassesCooldown = false;
		protected boolean hasAuraTax = true;
		protected @Nullable Element linkedElement = null;
		protected boolean reverseLinkedElement = false;
		protected Predicate<ElementalApplication> linkDecayOnlyIf = entity -> true;

		/**
		 * Creates a new, empty instance of {@code ElementSettings}.
		 */
		public static ElementSettings create() {
			return new ElementSettings();
		}

		/**
		 * Sets the texture of the element.
		 * @param texture The texture of the element.
		 */
		public ElementSettings setTexture(Identifier texture) {
			this.texture = texture;

			return this;
		}

		/**
		 * Sets the damage color of the element.
		 * @param texture The damage color of the element.
		 */
		public ElementSettings setDamageColor(Color damageColor) {
			this.damageColor = damageColor;

			return this;
		}

		/**
		 * Controls the priority of this element over the others. <br> <br>
		 *
		 * An element's <b>priority</b> dictates when it can be applied, reapplied, reacted
		 * with or when it is rendered on top of the entity. Element priority uses natural
		 * ordering, also known as ascending order or "least to greatest". <br> <br>
		 *
		 * For more information, you may refer to the methods that use Element priority.
		 *
		 * @param priority The priority of this element.
		 * @see ElementComponent#getPrioritizedElements() ElementComponent#getPrioritizedElements
		 * @see ElementComponentImpl#triggerReactions(ElementalApplication, net.minecraft.entity.LivingEntity) ElementComponentImpl#triggerReactions
		 * @see ElementComponentImpl#attemptReapply(ElementalApplication) ElementComponentImpl#attemptReapply
		 */
		public ElementSettings setPriority(int priority) {
			this.priority = priority;

			return this;
		}

		/**
		 * Sets the function controlling the decay rate of this element. <br> <br>
		 *
		 * This function must output a number {@code x} such that {@code x} is the amount of Gauge
		 * Units deducted per tick.
		 *
		 * @param texture The damage color of the element.
		 */
		public ElementSettings setDecayRate(@NotNull Function<ElementalApplication, Number> decayRate) {
			this.decayRate = decayRate;

			return this;
		}

		/**
		 * Sets if the element can be an Aura Element.
		 * @param aura If the element can be an Aura Element.
		 */
		public ElementSettings canBeAura(boolean aura) {
			this.canBeAura = aura;

			return this;
		}

		/**
		 * Sets if the element, when applied as an Aura Element, would have its Gauge Units
		 * deducted by the <a href="https://genshin-impact.fandom.com/wiki/Elemental_Gauge_Theory#Aura_Tax">Aura Tax</a>.
		 * @param auraTax If the element's Gauge Units should be deducted by the Aura Tax.
		 */
		public ElementSettings hasAuraTax(boolean auraTax) {
			this.hasAuraTax = auraTax;

			return this;
		}

		/**
		 * Sets if the Elemental Application tied to this element is tied to can bypass <a href="https://genshin-impact.fandom.com/wiki/Internal_Cooldown">Internal Cooldown</a>.
		 * @param bypassesCooldown If the Elemental Application tied to this element can bypass Internal Cooldown.
		 */
		public ElementSettings bypassesCooldown(boolean bypassesCooldown) {
			this.bypassesCooldown = bypassesCooldown;

			return this;
		}

		/**
		 * Sets if the element has <a href="https://genshin-impact.fandom.com/wiki/Elemental_Gauge_Theory#Decay_Rate_Inheritance">decay rate inheritance</a>.
		 * @param decayInheritance If the element has decay rate inheritance.
		 */
		public ElementSettings decayInheritance(boolean decayInheritance) {
			this.decayInheritance = decayInheritance;

			return this;
		}

		/**
		 * Links this element to the provided {@code element}. <br> <br>
		 *
		 * Upon linking, <i>gauge reduction</i> not originating from the gauge decay will be
		 * "synced" to the gauge units of this element, if it exists. <br> <br>
		 *
		 * Do note that linking isn't a "recursive" operation, i.e. Element A linked to Element B,
		 * Element B linked to Element C, reduction on Element A, B and C. <br> <br>
		 *
		 * You may also choose to sync the gauge decay permanently or with a {@code Predicate}.
		 *
		 * @param element The {@link Element} to link this element to.
		 * @see {@link ElementSettings#linkToElement(Element) ElementSettings#linkToElement} For linking the specified {@code element} to <b>this</b> element.
		 */
		public ElementSettings linkToElement(Element element) {
			this.linkedElement = element;

			return this;
		}

		/**
		 * Links the provided {@code element} to this element. <br> <br>
		 *
		 * Upon linking, <i>gauge reduction</i> not originating from the gauge decay will be
		 * "synced" to the gauge units of the provided {@code element}, if it exists. <br> <br>
		 *
		 * Do note that linking isn't a "recursive" operation, i.e. Element A linked to Element B,
		 * Element B linked to Element C, reduction on Element A, B and C. <br> <br>
		 *
		 * You may also choose to sync the gauge decay permanently or with a {@code Predicate}.
		 *
		 * @param element The {@link Element} to link to this element.
		 * @param boolean Whether the provided {@code element} is linked to this element instead.
		 * @see {@link ElementSettings#linkToElement(Element) ElementSettings#linkToElement} For linking <b>this</b> element to the specified {@code element}.
		 */
		@SuppressWarnings("unused")
		public ElementSettings linkElement(Element element) {
			this.linkedElement = element;
			this.reverseLinkedElement = true;

			return this;
		}

		/**
		 * Sets whether the gauge decay is linked to the gauge of this element, or the gauge of the
		 * corresponding element, if {@code reverse} was {@code true} for
		 * {@link ElementSettings#linkElement(Element, boolean) ElementSettings#linkedElement}.
		 *
		 * @param link Whether or not the gauge decay is also linked.
		 */
		@SuppressWarnings("unused")
		public ElementSettings linkGaugeDecay(boolean link) {
			return this.linkGaugeDecayIf(a -> link);
		}

		/**
		 * Sets whether, at this instance in time, the gauge decay is linked to the gauge of this
		 * element, or the gauge of the corresponding element, if {@code reverse} was {@code true}
		 * for {@link ElementSettings#linkElement(Element, boolean) ElementSettings#linkedElement}.
		 *
		 * @param predicate A {@code Predicate} indicating whether or not the gauge decay is also linked at this instance in time. The passed {@code ElementalApplication} is the one belonging to the linked element, or more formally, the element this element is linked to.
		 */
		public ElementSettings linkGaugeDecayIf(Predicate<ElementalApplication> predicate) {
			this.linkDecayOnlyIf = predicate;

			return this;
		}
	}

	private static class Decays {
		private static final Function<ElementalApplication, Number> NO_DECAY_RATE = a -> {
			return 0;
		};

		private static final Function<ElementalApplication, Number> DENDRO_DECAY_RATE = application -> {
			final ElementComponent component = ElementComponent.KEY.get(application.getEntity());

			return component.hasElementalApplication(Element.valueOf("BURNING"))
				// max(0.4, Natural Decay Rate_Dendro Aura × 2)
				// 0.04 is in GU/s, convert to GU/tick
				? Math.max(0.02, application.getDefaultDecayRate() * 2)
				: application.getDefaultDecayRate();
		};
	}
}
