package io.github.xrickastley.originsgenshin.element;

import java.util.UUID;

import io.github.xrickastley.originsgenshin.element.reaction.AbstractBurningElementalReaction;
import io.github.xrickastley.originsgenshin.exception.ElementalApplicationOperationException;
import io.github.xrickastley.originsgenshin.exception.ElementalApplicationOperationException.Operation;
import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;

/**
 * A class representing an Elemental Application for an entity.
 */
public abstract sealed class ElementalApplication permits DurationElementalApplication, GaugeUnitElementalApplication {
	protected final Type type;
	protected final Element element;
	protected final LivingEntity entity;
	protected final boolean isAura;
	// Used in uniquely identifying Elemental Applications.
	protected final UUID uuid;
	protected double gaugeUnits;
	protected double currentGauge;

	ElementalApplication(Type type, LivingEntity entity, Element element, UUID uuid, double gaugeUnits, boolean isAura) {
		this.type = type;
		this.entity = entity;
		this.element = element;
		this.isAura = isAura;
		this.uuid = uuid;

		this.gaugeUnits = gaugeUnits;
		this.currentGauge = gaugeUnits;
	}

	/**
	 * Gets the Type of this Elemental Application.
	 */
	public Type getType() {
		return this.type;
	}

	/**
	 * Gets the {@code Element} of to this Elemental Application.
	 */
	public Element getElement() {
		return this.element;
	}

	public LivingEntity getEntity() {
		return this.entity;
	}

	public UUID getUuid() {
		return this.uuid;
	}

	public double getGaugeUnits() {
		return this.gaugeUnits;
	}

	public double getCurrentGauge() {
		return this.currentGauge;
	}

	/**
	 * Gets the default decay rate of this Elemental Application.
	 * @return The default decay rate of this Elemental Application in {@code GU/tick}.
	 */
	protected abstract double getDefaultDecayRate();

	public abstract int getRemainingTicks();

	/**
	 * Returns whether this Elemental Application is using Gauge Units.
	 */
	public boolean isGaugeUnits() {
		return this.type == Type.GAUGE_UNIT;
	}

	/**
	 * Returns whether this Elemental Application is using a specified duration.
	 */
	public boolean isDuration() {
		return this.type == Type.DURATION;
	}

	/**
	 * Checks if the element in this Elemental Application is of the given {@code element}.
	 * @param element The {@code Element} to compare with this Elemental Application.
	 */
	public boolean isOfElement(Element element) {
		return this.element == element;
	}

	/**
	 * Returns whether this Elemental Application is an aura element.
	 */
	public boolean isAuraElement() {
		return this.isAura;
	}

	/**
	 * Returns whether this Elemental Application is empty.
	 */
	public abstract boolean isEmpty();

	/**
	 * Reduces the amount of gauge units in this Elemental Application, then returns the eventual amount of gauge units reduced.
	 * @param gaugeUnits The amount of gauge units to reduce.
	 * @return The eventual amount of gauge units reduced.
	 */
	public double reduceGauge(double gaugeUnits) {
		final double previousValue = this.currentGauge;
		
		this.currentGauge = Math.max(this.currentGauge - gaugeUnits, 0);
	
		return previousValue - this.currentGauge;
	}

	public void tick() {
		AbstractBurningElementalReaction.mixin$reduceQuickenGauge(this);
	}

	/**
	 * Reapplies this Elemental Application, given that {@code element} is the same element as this {@code ElementalApplication}.
	 * @param element The element to reapply for this application.
	 * @param gaugeUnits The amount of Elemental Gauge Units to reapply.
	 */
	public void reapply(Element element, double gaugeUnits) {
		reapply(ElementalApplications.gaugeUnits(this.entity, element, gaugeUnits));
	}

	/**
	 * Reapplies this Elemental Application, given that {@code application} has the same element as this one.
	 * @param application The Elemental Application to reapply using this application.
	 */
	public abstract void reapply(ElementalApplication application);

	public abstract ElementalApplication asAura();

	public abstract ElementalApplication asNonAura();
	
	public abstract NbtCompound asNbt();

	public void updateFromNbt(NbtElement nbt, long syncedAt) {
		if (!(nbt instanceof final NbtCompound compound)) throw new ElementalApplicationOperationException(Operation.INVALID_NBT_DATA, null, null);

		final ElementalApplication application = ElementalApplications.fromNbt(entity, compound, syncedAt);

		if (!application.uuid.equals(this.uuid)) throw new ElementalApplicationOperationException(Operation.INVALID_UUID_VALUES, this, application);

		if (application.type != this.type) throw new ElementalApplicationOperationException(Operation.REAPPLICATION_INVALID_TYPES, this, application);

		if (application.element != this.element) throw new ElementalApplicationOperationException(Operation.REAPPLICATION_INVALID_ELEMENT, this, application);

		this.currentGauge = application.currentGauge;
		this.gaugeUnits = application.gaugeUnits;
	}

	public static enum Type {
		// Has a specified amount of Gauge Units that decay over time.
		GAUGE_UNIT,
		// Has a specified amount of Gauge Units that are removed after DURATION.
		DURATION;
	}
}
