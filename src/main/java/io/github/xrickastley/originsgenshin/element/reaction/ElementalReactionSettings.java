package io.github.xrickastley.originsgenshin.element.reaction;

import javax.annotation.Nullable;

import io.github.xrickastley.originsgenshin.element.Element;
import io.github.xrickastley.originsgenshin.element.ElementalApplication.Type;
import net.minecraft.particle.DefaultParticleType;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;

public final class ElementalReactionSettings {
	protected final String name;
	protected final Identifier id;
	protected final @Nullable DefaultParticleType particle;
	protected double reactionCoefficient = 1.0;
	protected Pair<Element, Integer> auraElement;
	protected Pair<Element, Integer> triggeringElement;
	protected boolean reversable = false;
	protected boolean allowChildElements = false;
	protected boolean applyResultAsAura = false;

	public ElementalReactionSettings(String name, Identifier id, @Nullable DefaultParticleType particle) {
		this.name = name;
		this.id = id;
		this.particle = particle;
	}

	/**
	 * Sets the reaction coefficient of the Elemental Reaction. This is a multiplier that dictates how many gauge units are
	 * consumed from the aura element.
	 * @param reactionCoefficient The reaction coefficient of the Elemental Reaction.
	 */
	public ElementalReactionSettings setReactionCoefficient(double reactionCoefficient) {
		this.reactionCoefficient = reactionCoefficient;

		return this;
	}

	public ElementalReactionSettings setAuraElement(Element auraElement) {
		return setAuraElement(auraElement, -1);
	}

	/**
	 * Sets the Aura Element of the Elemental Reaction.
	 * @param auraElement The Aura Element of the Elemental Reaction.
	 * @param priority The priority of this reaction triggering when {@code auraElement} is one of the currently applied aura elements.
	 */
	public ElementalReactionSettings setAuraElement(Element auraElement, int priority) {
		this.auraElement = new Pair<Element,Integer>(auraElement, priority);

		return this;
	}

	public ElementalReactionSettings setTriggeringElement(Element triggeringElement) {
		return setTriggeringElement(triggeringElement, -1);
	}

	/**
	 * Sets the Triggering Element of the Elemental Reaction.
	 * @param triggeringElement The Triggering Element of the Elemental Reaction.
	 * @param priority The priority of this reaction triggering when {@code triggeringElement} is one of the currently applied aura 
	 * elements. {@code priority} will only be applied when {@code reversable} is true, as that is the only instance the 
	 * {@code triggeringElement} can be considered an aura element.
	 */
	public ElementalReactionSettings setTriggeringElement(Element triggeringElement, int priority) {
		this.triggeringElement = new Pair<Element,Integer>(triggeringElement, priority);

		return this;
	}

	/**
	 * Sets the Elemental Reaction as reversable. When this is {@code true}, {@code triggeringElement} can be considered as an aura
	 * element.
	 * @param reversable Whether or not the Elemental Reaction is reversable.
	 */
	public ElementalReactionSettings reversable(boolean reversable) {
		this.reversable = reversable;

		return this;
	}

	/**
	 * Whether or not the Elemental Reaction allows child elements. <br> <br>
	 * 
	 * When an Element is applied and possible elemental reactions are being searched for, 
	 * a child element will share the same priority set for it's parent element.
	 * 
	 * @param allowChildElements Whether or not the Elemental Reaction allows child elements. 
	 */
	public ElementalReactionSettings allowChildElements(boolean allowChildElements) {
		this.allowChildElements = allowChildElements;

		return this;
	}

	/**
	 * Whether or not the Trigger Element is applied as an aura. <br> <br>
	 * 
	 * Once all possible Elemental Reactions have been triggered, the triggering element
	 * may have some Gauge Units left. This setting allows for the remaining Gauge Units
	 * to be applied as an Elemental Aura. <br> <br>
	 * 
	 * If multiple Elemental Reactions are triggered, all triggered Elemental Reactions 
	 * must have this setting set to {@code true} for the Gauge Units from the triggering
	 * element to be applied as an Elemental Aura. <br> <br>
	 * 
	 * Do note that this setting will not affect Elemental Applications with {@link Type#DURATION},
	 * as those are always applied as an Aura Element after possible reactions.
	 * 
	 * @param applyResultAsAura Whether or not the remaining Gauge Units from the triggering element are applied as an Elemental Aura.
	 */
	public ElementalReactionSettings applyResultAsAura(boolean applyResultAsAura) {
		this.applyResultAsAura = applyResultAsAura;

		return this;
	}
}