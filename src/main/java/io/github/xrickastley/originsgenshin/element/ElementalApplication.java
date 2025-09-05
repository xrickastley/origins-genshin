package io.github.xrickastley.originsgenshin.element;

import java.util.Objects;
import java.util.UUID;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataType;
import io.github.apace100.calio.data.SerializableDataTypes;
import io.github.xrickastley.originsgenshin.data.OriginsGenshinDataTypes;
import io.github.xrickastley.originsgenshin.element.reaction.AbstractBurningElementalReaction;
import io.github.xrickastley.originsgenshin.exception.ElementalApplicationOperationException.Operation;
import io.github.xrickastley.originsgenshin.exception.ElementalApplicationOperationException;

import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.util.dynamic.Codecs;

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
	protected long appliedAt;

	ElementalApplication(Type type, LivingEntity entity, Element element, UUID uuid, double gaugeUnits, boolean isAura) {
		this.type = type;
		this.entity = entity;
		this.element = element;
		this.isAura = isAura;
		this.uuid = uuid;

		this.gaugeUnits = gaugeUnits;
		this.currentGauge = gaugeUnits;
		this.appliedAt = entity.getWorld().getTime();
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

	public long getAppliedAt() {
		return this.appliedAt;
	}

	/**
	 * Gets the number of ticks this Elemental Application has been applied for.
	 */
	public long getAppliedTicks() {
		return this.entity.getWorld().getTime() - this.appliedAt;
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
		this.element.reduceLinkedElements(previousValue - this.currentGauge, this, false);

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

	public NbtCompound asNbt() {
		final NbtCompound nbt = new NbtCompound();

		nbt.putString("Type", this.type.toString());
		nbt.putString("Element", this.element.toString());
		nbt.putUuid("UUID", uuid);
		nbt.putBoolean("IsAura", this.isAura);
		nbt.putDouble("GaugeUnits", this.gaugeUnits);
		nbt.putDouble("CurrentGauge", this.currentGauge);
		nbt.putLong("AppliedAt", this.appliedAt);

		return nbt;
	}

	public void updateFromNbt(NbtElement nbt, long syncedAt) {
		if (!(nbt instanceof final NbtCompound compound)) throw new ElementalApplicationOperationException(Operation.INVALID_NBT_DATA, null, null);

		final ElementalApplication application = ElementalApplications.fromNbt(entity, compound, syncedAt);

		if (!application.uuid.equals(this.uuid)) throw new ElementalApplicationOperationException(Operation.INVALID_UUID_VALUES, this, application);

		if (application.type != this.type) throw new ElementalApplicationOperationException(Operation.REAPPLICATION_INVALID_TYPES, this, application);

		if (application.element != this.element) throw new ElementalApplicationOperationException(Operation.REAPPLICATION_INVALID_ELEMENT, this, application);

		this.currentGauge = application.currentGauge;
		this.gaugeUnits = application.gaugeUnits;
		this.appliedAt = application.appliedAt;
	}

	public static enum Type {
		// Has a specified amount of Gauge Units that decay over time.
		GAUGE_UNIT,
		// Has a specified amount of Gauge Units that are removed after DURATION.
		DURATION;

		public static final Codec<Type> CODEC = Codecs.NON_EMPTY_STRING.xmap(Type::valueOf, Type::toString);
	}

	public static final class Builder {
		public static final Codec<ElementalApplication.Builder> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Type.CODEC.optionalFieldOf("type", ElementalApplication.Type.GAUGE_UNIT).forGetter(i -> i.type),
			Element.CODEC.fieldOf("element").forGetter(i -> i.element),
			Codec.BOOL.optionalFieldOf("aura", true).forGetter(i -> i.isAura),
			Codec.DOUBLE.fieldOf("gauge_units").forGetter(i -> i.gaugeUnits),
			Codec.DOUBLE.optionalFieldOf("duration", -1.0).forGetter(i -> i.duration)
		).apply(instance, ElementalApplication.Builder::new));

		public static final SerializableDataType<ElementalApplication.Builder> DATA
			= SerializableDataType.compound(
				ElementalApplication.Builder.class,
				new SerializableData()
					.add("type", OriginsGenshinDataTypes.ELEMENTAL_APPLICATION_TYPE, ElementalApplication.Type.GAUGE_UNIT)
					.add("element", OriginsGenshinDataTypes.ELEMENT)
					.add("aura", SerializableDataTypes.BOOLEAN, true)
					.add("gauge_units", SerializableDataTypes.DOUBLE)
					.add("duration", SerializableDataTypes.DOUBLE, -1.0),
				dataInst -> ElementalApplications.builder()
					.setType(dataInst.get("type"))
					.setElement(dataInst.get("element"))
					.setAsAura(dataInst.getBoolean("aura"))
					.setGaugeUnits(dataInst.getDouble("gauge_units"))
					.setDuration(dataInst.getDouble("duration")),
				(data, inst) -> {
					final SerializableData.Instance dataInst = data.new Instance();
					dataInst.set("type", inst.type);
					dataInst.set("element", inst.element);
					dataInst.set("aura", inst.isAura);
					dataInst.set("gauge_units", inst.gaugeUnits);
					dataInst.set("duration", inst.duration);
					return dataInst;
				}
			);

		private Type type;
		private Element element;
		private boolean isAura;
		private double gaugeUnits;
		private double duration;

		Builder() {
			this.isAura = true;
		}

		private Builder(Type type, Element element, boolean isAura, double gaugeUnits, double duration) {
			this.type = type;
			this.element = element;
			this.isAura = isAura;
			this.gaugeUnits = gaugeUnits;
			this.duration = duration;
		}

		public Builder setType(Type type) {
			this.type = type;

			return this;
		}

		public Builder setElement(Element element) {
			this.element = element;

			return this;
		}

		public Builder setAsAura(boolean isAura) {
			this.isAura = isAura;

			return this;
		}

		public Builder setGaugeUnits(double gaugeUnits) {
			this.gaugeUnits = gaugeUnits;

			return this;
		}

		public Builder setDuration(double duration) {
			this.duration = duration;

			return this;
		}

		public ElementalApplication build(final LivingEntity entity) {
			this.type = Objects.requireNonNull(type);
			this.element = Objects.requireNonNull(element);
			this.gaugeUnits = Objects.requireNonNull(gaugeUnits);

			if (type == Type.DURATION) {
				this.duration = Objects.requireNonNull(duration);

				return ElementalApplications.duration(entity, element, gaugeUnits, duration);
			} else {
				return ElementalApplications.gaugeUnits(entity, element, gaugeUnits, isAura);
			}
		}

		public static SerializableData.Instance toData(SerializableData data, ElementalApplication.Builder instance) {
			SerializableData.Instance dataInst = data.new Instance();
			dataInst.set("type", instance.type);
			dataInst.set("element", instance.element);
			dataInst.set("aura", instance.isAura);
			dataInst.set("gauge_units", instance.gaugeUnits);
			dataInst.set("duration", instance.duration);
			return dataInst;
		}
	}
}
