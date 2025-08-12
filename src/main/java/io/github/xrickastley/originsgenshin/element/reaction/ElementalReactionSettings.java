package io.github.xrickastley.originsgenshin.element.reaction;

import io.github.xrickastley.originsgenshin.element.Element;
import io.github.xrickastley.originsgenshin.element.ElementalApplication.Type;

import net.minecraft.particle.DefaultParticleType;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;

import java.util.HashSet;
import java.util.Set;

import javax.annotation.Nullable;

public final class ElementalReactionSettings {
	final String name;
	final Identifier id;
	final @Nullable DefaultParticleType particle;
	double reactionCoefficient = 1.0;
	Pair<Element, Integer> auraElement;
	Pair<Element, Integer> triggeringElement;
	boolean reversable = false;
	boolean applyResultAsAura = false;
	boolean endsReactionTrigger = false;
	boolean preventsPriorityUpgrade = false;
	Set<Identifier> preventsReactionsAfter = new HashSet<>();

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

	public ElementalReactionSettings setAuraElement(Element element) {
		return setAuraElement(element, -1);
	}

	/**
	 * Sets the Aura Element of the Elemental Reaction. <br> <br>
	 *
	 * This is the element that <b>must</b> be applied onto the entity in order for the reaction to
	 * be triggered by applying the Triggering Element onto the entity. <br> <br>
	 *
	 * However, when the Elemental Reaction is considered "reversable" through
	 * {@link ElementalReactionSettings#reversable(boolean) ElementalReactionSettings#reversable()},
	 * the Aura Element may be considered as the Triggering Element, and the Triggering Element
	 * may be considered as the Aura Element.
	 *
	 * @param element The Aura Element of the Elemental Reaction.
	 * @param priority The priority of this reaction triggering when {@code auraElement} is currently
	 * the triggering element. {@code priority} will only be applied when {@code reversable} is
	 * {@code true}, as that is the only instance the {@code auraElement} can be considered a
	 * triggering element.
	 */
	public ElementalReactionSettings setAuraElement(Element element, int priority) {
		this.auraElement = new Pair<Element,Integer>(element, priority);

		return this;
	}

	public ElementalReactionSettings setTriggeringElement(Element element) {
		return setTriggeringElement(element, -1);
	}

	/**
	 * Sets the Triggering Element of the Elemental Reaction. <br> <br>
	 *
	 * This is the element that <b>must</b> be applied onto the entity with the specified Aura
	 * Element in order for the reaction to be triggered. <br> <br>
	 *
	 * However, when the Elemental Reaction is considered "reversable" through
	 * {@link ElementalReactionSettings#reversable(boolean) ElementalReactionSettings#reversable()},
	 * the Triggering Element may be considered as the Aura Element, and the Aura Element may be
	 * considered as the Triggering Element.
	 *
	 * @param element The Triggering Element of the Elemental Reaction.
	 * @param priority The priority of this reaction triggering when {@code triggeringElement} is the triggering element.
	 */
	public ElementalReactionSettings setTriggeringElement(Element element, int priority) {
		this.triggeringElement = new Pair<Element,Integer>(element, priority);

		return this;
	}

	/**
	 * Sets the Elemental Reaction as reversable. <br> <br>
	 *
	 * When this is {@code true}, the <b>Triggering Element</b> can be considered as an <b>Aura
	 * Element</b>.
	 *
	 * @param reversable Whether the Elemental Reaction is reversable.
	 */
	public ElementalReactionSettings reversable(boolean reversable) {
		this.reversable = reversable;

		return this;
	}

	/**
	 * Whether the triggering Element is applied as an aura. <br> <br>
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
	 * @param applyResultAsAura Whether the remaining Gauge Units from the triggering
	 * element are applied as an Elemental Aura.
	 */
	public ElementalReactionSettings applyResultAsAura(boolean applyResultAsAura) {
		this.applyResultAsAura = applyResultAsAura;

		return this;
	}

	/**
	 * Whether this reaction ends all future reactions from triggering. <br> <br>
	 *
	 * Once a reaction is triggered, an attempt to trigger another is made. This setting denies
	 * other reactions to be triggered after triggering this reaction.
	 *
	 * @param endsReactionTrigger Whether reactions can be triggered after this reaction.
	 */
	public ElementalReactionSettings endsReactionTrigger(boolean endsReactionTrigger) {
		this.endsReactionTrigger = endsReactionTrigger;

		return this;
	}

	/**
	 * Whether this reaction prevents the priority upgrade. <br> <br>
	 *
	 * Once a reaction is triggered, an attempt to trigger another is made. If no reactions were
	 * found, an attempt to upgrade the "element priority" is done. This setting denies that
	 * attempt after triggering this reaction. <br> <br>
	 *
	 * However, the attempt to upgrade the priority will only be denied <b>once</b> after this
	 * reaction. Succeeding reactions <b>must</b> also have this property enabled in order for the
	 * upgrade to be <i>fully</i> denied.
	 *
	 * @param preventsPriorityUpgrade Whether the element priority can be upgraded after
	 * this reaction.
	 */
	public ElementalReactionSettings preventsPriorityUpgrade(boolean preventsPriorityUpgrade) {
		this.preventsPriorityUpgrade = preventsPriorityUpgrade;

		return this;
	}

	/**
	 * Whether this reaction prevents other reactions from triggering after it.
	 * 
	 * This setting denies the specified reactions from triggering <b>directly after</b> this
	 * reaction.
	 * 
	 * @param reactions The reactions to prevent from triggering <b>directly after</b> this reaction.
	 */
	public ElementalReactionSettings preventsReactionsAfter(Identifier ...reactions) {
		this.preventsReactionsAfter.addAll(Set.of(reactions));

		return this;
	}
}