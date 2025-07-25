package io.github.xrickastley.originsgenshin.element;

import java.util.ArrayList;
import java.util.function.Function;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import io.github.xrickastley.originsgenshin.OriginsGenshin;
import io.github.xrickastley.originsgenshin.component.ElementComponent;
import io.github.xrickastley.originsgenshin.component.ElementComponentImpl;
import io.github.xrickastley.originsgenshin.util.Color;
import io.github.xrickastley.originsgenshin.util.Colors;

import net.minecraft.util.Identifier;

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
	FROZEN(
		OriginsGenshin.identifier("frozen"),
		ElementSettings.create()
			.setTexture(OriginsGenshin.identifier("textures/element/frozen.png"))
			.setDamageColor(Color.fromRGBAHex("#b4ffff"))
			.setParentElement(Element.CRYO)
			.setPriority(1)
			.bypassesCooldown(true)
	),
	QUICKEN(
		OriginsGenshin.identifier("quicken"),
		ElementSettings.create()
			.setTexture(OriginsGenshin.identifier("textures/element/quicken.png"))
			.setDamageColor(Color.fromRGBAHex("#01e858"))
			.setParentElement(Element.DENDRO)
			.setPriority(1)
			.bypassesCooldown(true)
	),
	BURNING(
		OriginsGenshin.identifier("burning"),
		ElementSettings.create()
			.setTexture(OriginsGenshin.identifier("textures/element/burning.png"))
			.setDamageColor(Colors.PYRO)
			.setParentElement(Element.PYRO)
			.setPriority(1)
			.setDecayRate(Decays.NO_DECAY_RATE)
			.bypassesCooldown(true)
			.hasAuraTax(false)
			.excludesPriorityCheck(true)
	);

	private final Identifier id;
	private final ElementSettings settings;
	private final ArrayList<Element> children = new ArrayList<>();

	private Element(Identifier id, ElementSettings settings) {
		this.id = id;
		this.settings = settings;

		if (settings.parentElement != null) settings.parentElement.children.add(this);
	}

	public boolean hasDecayInheritance() {
		return settings.decayInheritance;
	}

	public boolean hasParentElement() {
		return settings.parentElement != null;
	}

	public Element getParentElement() {
		return settings.parentElement;
	}

	public boolean hasTexture() {
		return settings.texture != null || (settings.parentElement != null && settings.parentElement.hasTexture());
	}

	public Identifier getTexture() {
		return settings.texture != null
			? settings.texture
			: settings.parentElement != null
				? settings.parentElement.getTexture()
				: null;
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

	public ArrayList<Element> getChildrenElements() {
		// Prevents modification to the original array.
		return new ArrayList<>(this.children);
	}

	/**
	 * Checks if the provided {@code element} is a first-child of this {@link Element}. <br> <br>
	 *
	 * This does not work recursively, meaning that a child-of-child element will not be considered
	 * as a child of this element.
	 *
	 * @param element The element to check.
	 * @return Whether {@code element} is a first-child of this {@link Element}.
	 */
	public boolean isChild(Element element) {
		return this.children
			.stream()
			.anyMatch(childElement -> childElement == element);
	}

	public boolean hasAuraTax() {
		return settings.hasAuraTax;
	}

	public boolean excludesPriorityCheck() {
		return settings.excludesPriorityCheck;
	}

	/**
	 * A class used in creating data for Elements, instead of multiple overloaded constructors.
	 */
	private static class ElementSettings {
		private ElementSettings() {};

		protected Identifier texture;
		protected Color damageColor;
		protected int priority;
		protected @Nullable Element parentElement;
		protected @Nullable Function<ElementalApplication, Number> decayRate = null;
		protected boolean canBeAura = true;
		protected boolean decayInheritance = true;
		protected boolean bypassesCooldown = false;
		protected boolean hasAuraTax = true;
		protected boolean excludesPriorityCheck = false;

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
		 * Sets the element as a child of {@code parentElement}. <br> <br>
		 *
		 * A child element is able to be used in-place of it's parent element in an Elemental
		 * Reaction if it has {@code allowChildElements} set to {@code true}.
		 *
		 * @param parentElement The parent element of the element.
		 * @deprecated This setting does nothing.
		 */
		@Deprecated
		public ElementSettings setParentElement(Element parentElement) {
			this.parentElement = parentElement;

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
		 * Sets if the element excludes itself from the priority check. <br> <br>
		 *
		 * When elements are applied and no reactions are triggered, an attempt is made to make
		 * the element an Aura element, allowing for "double auras" to exist. <br> <br>
		 *
		 * If an element with a <b>higher</b> priority exists as an Aura Element, elements with
		 * lower priorities may <b>not</b> be applied while that element is currently applied as
		 * an Aura Element. <br> <br>
		 *
		 * This setting changes whether the element is included as a "higher priority"
		 * element upon checking. If <b>all</b> currently applied Aura elements with the "higher
		 * priority" are excluded, the next highest priority elements will be considered.
		 */
		public ElementSettings excludesPriorityCheck(boolean priorityCheck) {
			this.excludesPriorityCheck = priorityCheck;

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