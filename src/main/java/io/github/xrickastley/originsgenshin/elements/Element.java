package io.github.xrickastley.originsgenshin.elements;

import java.util.ArrayList;

import io.github.xrickastley.originsgenshin.OriginsGenshin;
import io.github.xrickastley.originsgenshin.util.Color;
import io.github.xrickastley.originsgenshin.util.Colors;

import net.minecraft.util.Identifier;

public enum Element {
	PYRO(
		OriginsGenshin.identifier("pyro"),
		ElementSettings
			.create()
			.setTexture(OriginsGenshin.identifier("textures/particle/pyro.png"))
			.setDamageColor(Colors.PYRO)
			.setDecayInheritance(false)
			.setPriority(2)
	),
	HYDRO(
		OriginsGenshin.identifier("hydro"),
		ElementSettings
			.create()
			.setTexture(OriginsGenshin.identifier("textures/particle/hydro.png"))
			.setDamageColor(Colors.HYDRO)
			.setPriority(2)
	),
	ANEMO(
		OriginsGenshin.identifier("anemo"),
		ElementSettings
			.create()
			.setTexture(OriginsGenshin.identifier("textures/particle/anemo.png"))
			.setDamageColor(Colors.ANEMO)
			.setAsAura(false)
			.setPriority(2)
	),
	ELECTRO(
		OriginsGenshin.identifier("electro"),
		ElementSettings
			.create()
			.setTexture(OriginsGenshin.identifier("textures/particle/electro.png"))
			.setDamageColor(Colors.ELECTRO)
			.setPriority(2)
	),
	DENDRO(
		OriginsGenshin.identifier("dendro"),
		ElementSettings
			.create()
			.setTexture(OriginsGenshin.identifier("textures/particle/dendro.png"))
			.setDamageColor(Colors.DENDRO)
			.setPriority(2)
	),
	CRYO(
		OriginsGenshin.identifier("cryo"),
		ElementSettings
			.create()
			.setTexture(OriginsGenshin.identifier("textures/particle/cryo.png"))
			.setDamageColor(Colors.CRYO)
			.setPriority(2)
	),
	GEO(
		OriginsGenshin.identifier("geo"),
		ElementSettings
			.create()
			.setTexture(OriginsGenshin.identifier("textures/particle/geo.png"))
			.setDamageColor(Colors.GEO)
			.setAsAura(false)
			.setPriority(2)
	),
	FROZEN(
		OriginsGenshin.identifier("frozen"),
		ElementSettings.create()
			.setParentElement(Element.CRYO)
			.setAsBypassesCooldown(true)
			.setPriority(1)
	),
	QUICKEN(
		OriginsGenshin.identifier("quicken"),
		ElementSettings.create()
			.setParentElement(Element.DENDRO)
			.setAsBypassesCooldown(true)
			.setPriority(1)
	),
	BURNING(
		OriginsGenshin.identifier("burning"),
		ElementSettings.create()
			.setParentElement(Element.PYRO)
			.setAsBypassesCooldown(true)
			.setPriority(1)
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
		return settings.isAura;
	}
	
	public int getPriority() {
		return this.settings.priority;
	}

	public boolean bypassesInternalCooldown() {
		return this.settings.bypassesCooldown;
	}

	public ArrayList<Element> getChildrenElements() {
		return this.children;
	}

	/**
	 * A class used in creating data for Elements, instead of multiple overloaded constructors.
	 */
	private static class ElementSettings {
		private ElementSettings() {};

		protected Identifier texture;
		protected Color damageColor;
		protected boolean isAura = true;
		protected boolean decayInheritance = true;
		protected boolean bypassesCooldown = false;
		protected Element parentElement;
		protected int priority;

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
		 * Sets the priority of this element.
		 * @param priority The priority of this element.
		 */
		public ElementSettings setPriority(int priority) {
			this.priority = priority;
	
			return this;
		}

		/**
		 * Sets if the element can be an Aura Element.
		 * @param isAura If the element can be an Aura Element.
		 */
		public ElementSettings setAsAura(boolean isAura) {
			this.isAura = isAura;
	
			return this;
		}
	
		/**
		 * Sets if the Elemental Application tied to this element is tied to can bypass <a href="https://genshin-impact.fandom.com/wiki/Internal_Cooldown">Internal Cooldown</a>.
		 * @param bypassesCooldown If the Elemental Application tied to this element can bypass Internal Cooldown.
		 */
		public ElementSettings setAsBypassesCooldown(boolean bypassesCooldown) {
			this.bypassesCooldown = bypassesCooldown;

			return this;
		}

		/**
		 * Sets if the element has <a href="https://genshin-impact.fandom.com/wiki/Elemental_Gauge_Theory#Decay_Rate_Inheritance">decay rate inheritance</a>.
		 * @param decayInheritance If the element has decay rate inheritance.
		 */
		public ElementSettings setDecayInheritance(boolean decayInheritance) {
			this.decayInheritance = decayInheritance;
	
			return this;
		}
		
		/**
		 * Sets the element as a child of {@code parentElement}. <br> <br>
		 * 
		 * A child element is able to be used in-place of it's parent element in an Elemental Reaction if
		 * it has {@code allowChildElements()} set to {@code true}. <br> <br>
		 * 
		 * Additionally, if a parent element and a child element co-exist as Elemental Auras and an 
		 * Elemental Reaction is triggered, the gauge units of the child element is consumed first before
		 * the gauge units of it's parent element.
		 *  
		 * @param parentElement The parent element of the element.
		 */
		public ElementSettings setParentElement(Element parentElement) {
			this.parentElement = parentElement;
	
			return this;
		}
	}
}