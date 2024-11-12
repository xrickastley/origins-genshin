package io.github.xrickastley.originsgenshin.element;

import java.util.UUID;

import io.github.xrickastley.originsgenshin.component.ElementComponent;
import io.github.xrickastley.originsgenshin.error.ElementalApplicationOperationException;
import io.github.xrickastley.originsgenshin.error.ElementalApplicationOperationException.Operation;
import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.util.math.MathHelper;

/**
 * A class representing an Elemental Application for an entity.
 */
public final class ElementalApplication {
	protected final LivingEntity entity;
	// Used in uniquely identifying Elemental Applications.
	protected final UUID uuid;
	protected final Element element;
	protected final boolean aura;
	protected final Type type;
	protected double gaugeUnits;
	protected double currentGauge;
	protected double duration;
	protected int appliedAt;

	private ElementalApplication(LivingEntity entity, Element element, UUID uuid, double gaugeUnits, boolean aura) {
		this.entity = entity;
		this.element = element;
		this.uuid = uuid;
		this.aura = aura;
		this.type = Type.GAUGE_UNITS;

		this.gaugeUnits = gaugeUnits;
		this.currentGauge = gaugeUnits;

		// Aura tax.
		if (this.aura) this.currentGauge *= 0.8;
	}

	private ElementalApplication(LivingEntity entity, Element element, UUID uuid, double gaugeUnits, double duration) {
		this.entity = entity;
		this.element = element;
		this.uuid = uuid;
		this.aura = true;
		this.type = Type.DURATION;

		this.duration = duration;
		this.appliedAt = entity.age;

		this.gaugeUnits = gaugeUnits;
		this.currentGauge = gaugeUnits;
	}
	
	/**
	 * Creates an Elemental Application using Elemental Gauge Units.
	 * @param entity The entity to create an Elemental Application for.
	 * @param element The Element of this Elemental Application.
	 * @param gaugeUnits The amount of Elemental Gauge Units this Elemental Application has.
	 */
	public static ElementalApplication usingGaugeUnits(LivingEntity entity, Element element, double gaugeUnits) {
		return ElementalApplication.usingGaugeUnits(entity, element, gaugeUnits, true);
	}

	/**
	 * Creates an Elemental Application using Elemental Gauge Units.
	 * @param entity The entity to create an Elemental Application for.
	 * @param element The Element of this Elemental Application.
	 * @param gaugeUnits The amount of Elemental Gauge Units this Elemental Application has.
	 * @param aura Whether or not this Elemental Application is an Aura Element. This means that the <a href="https://genshin-impact.fandom.com/wiki/Elemental_Gauge_Theory#Aura_Tax">Aura Tax</a> applies to the current gauge units of this Element.
	 */
	public static ElementalApplication usingGaugeUnits(LivingEntity entity, Element element, double gaugeUnits, boolean aura) {
		return new ElementalApplication(entity, element, UUID.randomUUID(), gaugeUnits, aura);
	}

	/**
	 * Creates an Elemental Application with a specified duration.
	 * @param entity The entity to create an Elemental Application for.
	 * @param element The Element of this Elemental Application.
	 * @param gaugeUnits The amount of Gauge Units this Elemental Application has.
	 * @param duration The duration of the Elemental Application, in ticks.
	 */
	public static ElementalApplication usingDuration(LivingEntity entity, Element element, double gaugeUnits, double duration) {
		return new ElementalApplication(entity, element, UUID.randomUUID(), gaugeUnits, duration);
	}

	/**
	 * Creates an Elemental Application from an NBT.
	 * @param entity The entity to create an Elemental Application for.
	 * @param nbt The NBT to create the Elemental Application from.
	 */
	public static ElementalApplication fromNbt(LivingEntity entity, NbtElement nbt, int sentAtAge) {
		if (!(nbt instanceof final NbtCompound compound)) throw new ElementalApplicationOperationException(Operation.INVALID_NBT_DATA, null, null);

		final Type type = Type.valueOf(compound.getString("type"));
		final Element element = Element.valueOf(compound.getString("element"));
		final UUID uuid = compound.getUuid("uuid");
		final double gaugeUnits = compound.getDouble("gaugeUnits");
		final double currentGauge = compound.getDouble("currentGauge");

		ElementalApplication application;

		if (type == Type.GAUGE_UNITS) {
			final boolean isAura = compound.getBoolean("isAura");

			application = new ElementalApplication(entity, element, uuid, gaugeUnits, isAura);
			
			final double syncedGaugeDeduction = Math.max(entity.age - sentAtAge, 0) * application.getDecayPerTick();

			application.currentGauge = MathHelper.clamp(currentGauge - syncedGaugeDeduction, 0, application.gaugeUnits);
		} else {
			final double duration = compound.getDouble("duration");
			final int appliedAt = compound.getInt("appliedAt");

			application = new ElementalApplication(entity, element, uuid, gaugeUnits, duration);
			application.currentGauge = currentGauge;
			application.appliedAt = appliedAt;
		}

		return application;
	}

	/**
	 * Gets the {@code Element} of to this Elemental Application.
	 */
	public Element getElement() {
		return element;
	}

	/**
	 * Gets the Type of this Elemental Application.
	 */
	public Type getType() {
		return this.type;
	}

	public double getGaugeUnits() {
		return gaugeUnits;
	}

	public double getCurrentGauge() {
		return this.currentGauge;
	}

	public double getDuration() {
		return duration;
	}

	public int getRemainingTicks() {
		if (type == Type.DURATION) {
			// System.out.printf("(%d + %.2f) - %d => %.2f\n", appliedAt, duration, entity.age, (appliedAt + duration) - entity.age);

			return (int) (appliedAt + duration) - entity.age;
		}

		// Currently in s/GU
		double decayRate = 35 / (4 * this.gaugeUnits) + (25 / 8.0);
		// Now in ticks/GU
		double decayRateTicks = decayRate / 0.05;
		double remainingTicks = decayRateTicks * this.currentGauge;

		return (int) remainingTicks;
	}

	public UUID getUuid() {
		return uuid;
	}

	/**
	 * Whether or not this Elemental Application is using Gauge Units.
	 */
	public boolean isUsingGaugeUnits() {
		return this.type == Type.GAUGE_UNITS;
	}

	/**
	 * Whether or not this Elemental Application is using a specified duration.
	 */
	public boolean isUsingDuration() {
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
	 * Whether or not this Elemental Application is an aura element.
	 */
	public boolean isAuraElement() {
		return this.aura;
	}

	/**
	 * Whether or not this Elemental Application should be removed. <br> <br>
	 * 
	 * This is {@code true}, 
	 * <ul>
	 * 	<li>For {@link Type#DURATION} when {@code duration + entity.age} reaches {@code appliedAt} or {@code gaugeUnits} reaches {@code 0} 
	 * 	<li>For {@link Type#GAUGE_UNITS} when {@code currentGauge} reaches {@code 0}.
	 * </ul>
	 */
	public boolean shouldBeRemoved() {
		return (isUsingDuration() && (entity.age >= (appliedAt + duration) || gaugeUnits <= 0)) || (isUsingGaugeUnits() && currentGauge <= 0);
	}

	/**
	 * Reduces the amount of gauge units in this Elemental Application, then returns the eventual amount of gauge units reduced.
	 * @param gaugeUnits The amount of gauge units to reduce.
	 * @return The eventual amount of gauge units reduced.
	 */
	public double reduceGauge(double gaugeUnits) {
		double difference;

		if (type == Type.GAUGE_UNITS) {
			final double previousValue = this.currentGauge;
		
			this.currentGauge = Math.max(this.currentGauge - gaugeUnits, 0);
	
			difference = previousValue - this.currentGauge;
		} else {
			final double previousValue = this.gaugeUnits;
		
			this.gaugeUnits = Math.max(this.gaugeUnits - gaugeUnits, 0);
	
			difference = previousValue - this.gaugeUnits;
		}
	
		ElementComponent.sync(entity);

		return difference;
	}

	public boolean matchesUUID(ElementalApplication other) {
		return this.uuid.equals(other.uuid);
	}

	public void tick() {
		decayApplication();
	}

	/**
	 * Reapplies this Elemental Application, given that {@code element} is the same element as this {@code ElementalApplication}.
	 * @param element The element to reapply for this application.
	 * @param gaugeUnits The amount of Elemental Gauge Units to reapply.
	 */
	public void reapply(Element element, double gaugeUnits) {
		reapply(ElementalApplication.usingGaugeUnits(this.entity, element, gaugeUnits));
	}

	/**
	 * Reapplies this Elemental Application, given that {@code application} has the same element as this one.
	 * @param application The Elemental Application to reapply using this application.
	 */
	public void reapply(ElementalApplication application) {
		if (application.element != this.element) throw new ElementalApplicationOperationException(Operation.REAPPLICATION_INVALID_ELEMENT, this, application);

		if (application.type != this.type) throw new ElementalApplicationOperationException(Operation.REAPPLICATION_INVALID_TYPES, this, application);

		if (this.isUsingGaugeUnits()) {
			// However, the current gauge, handled by currentGauge, is always the most of both applications.
			this.currentGauge = Math.max(this.gaugeUnits, application.gaugeUnits);
			// The decay rate, handled by gaugeUnits, is always the lesser of both applications.
			this.gaugeUnits = Math.min(this.gaugeUnits, application.gaugeUnits);
		} else {
			this.duration = Math.max(this.duration, application.duration);
			this.gaugeUnits = Math.max(this.gaugeUnits, application.gaugeUnits);
		}

		if (this.aura) this.currentGauge *= 0.8;
	
		ElementComponent.sync((LivingEntity) entity);
	}
	
	protected void decayApplication() {
		if (type == Type.DURATION) return;

		this.currentGauge -= getDecayPerTick();
	}

	/**
	 * Gets the current decay rate per tick, derived from <a href="https://genshin-impact.fandom.com/wiki/Elemental_Gauge_Theory#Aura_Duration_and_Decay_Rate">Elemental Gauge Theory: Aura Duration and Decay Rate</a>.
	 */
	protected double getDecayPerTick() {
		// Currently in s/GU
		double decayRate = (35 / (4 * this.gaugeUnits)) + (25.0 / 8.0);
		// Now in ticks/GU
		double decayRateTicks = decayRate / 0.05;
		// Now a GU/tick, allowing us to tick down the gauge easily.
		return 1 / decayRateTicks;
	}

	public NbtCompound asNbt() {
		final NbtCompound nbt = new NbtCompound();

		nbt.putString("type", this.type.toString());
		nbt.putString("element", this.element.toString());
		nbt.putUuid("uuid", uuid);
		nbt.putBoolean("isAura", this.aura);
		nbt.putDouble("gaugeUnits", this.gaugeUnits);
		nbt.putDouble("currentGauge", this.currentGauge);
		nbt.putDouble("duration", this.duration);
		nbt.putInt("appliedAt", this.appliedAt);

		return nbt;
	}

	public void updateFromNbt(NbtElement nbt, int sentAtAge) {
		if (!(nbt instanceof final NbtCompound compound)) throw new ElementalApplicationOperationException(Operation.INVALID_NBT_DATA, null, null);

		final ElementalApplication application = ElementalApplication.fromNbt(entity, compound, sentAtAge);

		if (!uuid.equals(this.uuid)) throw new ElementalApplicationOperationException(Operation.INVALID_UUID_VALUES, this, application);

		this.gaugeUnits = application.gaugeUnits;
		
		if (type == Type.GAUGE_UNITS) {
			this.currentGauge = application.currentGauge;
		} else {
			this.duration = application.duration;
			this.appliedAt = application.appliedAt;
		}
	}

	public static enum Type {
		// Has a specified amount of Gauge Units that decay over time.
		GAUGE_UNITS,
		// Has a specified amount of Gauge Units that are removed after DURATION.
		DURATION;
	}

	@Override
	public String toString() {
		return this.type == Type.GAUGE_UNITS
			? String.format(
				"%s@%s[type=GAUGE_UNITS, element=%s, gaugeUnits=%2f, currentGauge=%.2f]",
				this.getClass().getSimpleName(),
				Integer.toHexString(this.hashCode()),
				this.getElement().toString(),
				this.getGaugeUnits(),
				this.getCurrentGauge()
			)
			: String.format(
				"%s@%s[type=DURATION, element=%s, gaugeUnits=%s, duration=%.2f]",
				this.getClass().getSimpleName(),
				Integer.toHexString(this.hashCode()),
				this.getElement().toString(),
				this.getGaugeUnits(),
				this.getDuration()
			);
	}
}
