package io.github.xrickastley.originsgenshin.element;

import java.util.UUID;

import io.github.xrickastley.originsgenshin.component.ElementComponent;
import io.github.xrickastley.originsgenshin.exception.ElementalApplicationOperationException.Operation;
import io.github.xrickastley.originsgenshin.exception.ElementalApplicationOperationException;

import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.world.World;

public final class DurationElementalApplication extends ElementalApplication {
	private double duration;

	DurationElementalApplication(LivingEntity entity, Element element, UUID uuid, double gaugeUnits, double duration) {
		super(Type.DURATION, entity, element, uuid, gaugeUnits, true);

		this.duration = duration;
	}

	static ElementalApplication fromNbt(LivingEntity entity, NbtCompound nbt, long syncedAt) {
		final Element element = Element.valueOf(nbt.getString("Element"));
		final UUID uuid = nbt.getUuid("UUID");
		final double gaugeUnits = nbt.getDouble("GaugeUnits");
		final double duration = nbt.getDouble("Duration");

		final var application = new DurationElementalApplication(entity, element, uuid, gaugeUnits, duration);

		application.currentGauge = nbt.getDouble("CurrentGauge");
		application.appliedAt = nbt.getLong("AppliedAt");

		return application;
	}

	public double getDuration() {
		return this.duration;
	}

	@Override
	protected double getDefaultDecayRate() {
		return 0;
	}

	@Override
	public int getRemainingTicks() {
		return (int) (appliedAt + duration - entity.getWorld().getTime());
	}

	/**
	 * {@inheritDoc} <br> <br>
	 *
	 * This implementation guarantees this to be {@code true} when {@code currentGauge} reaches
	 * {@code 0} or when the current world time, given by {@link LivingEntity#getWorld()}
	 * {@link World#getTime() .getTime()} exceeds {@code duration + appliedAt}.
	 */
	@Override
	public boolean isEmpty() {
		return this.currentGauge <= 0 || entity.getWorld().getTime() >= (this.appliedAt + this.duration);
	}

	@Override
	public void reapply(ElementalApplication application) {
		if (application.element != this.element) throw new ElementalApplicationOperationException(Operation.REAPPLICATION_INVALID_ELEMENT, this, application);

		if (application.type != this.type || !(application instanceof final DurationElementalApplication durationApp)) throw new ElementalApplicationOperationException(Operation.REAPPLICATION_INVALID_TYPES, this, application);

		this.gaugeUnits = Math.max(this.gaugeUnits, application.gaugeUnits);
		this.currentGauge = gaugeUnits;
		this.duration = durationApp.duration;

		ElementComponent.sync(this.entity);
	}

	@Override
	public ElementalApplication asAura() {
		throw new UnsupportedOperationException("This method is unsupported on Elemental Applications with a DURATION type!");
	}

	@Override
	public ElementalApplication asNonAura() {
		throw new UnsupportedOperationException("This method is unsupported on Elemental Applications with a DURATION type!");
	}

	@Override
	public NbtCompound asNbt()	{
		final NbtCompound nbt = super.asNbt();

		nbt.putDouble("Duration", this.duration);

		return nbt;
	}

	@Override
	public void updateFromNbt(NbtElement nbt, long syncedAt) {
		super.updateFromNbt(nbt, syncedAt);

		final ElementalApplication application = ElementalApplications.fromNbt(entity, (NbtCompound) nbt, syncedAt);

		this.duration = ((DurationElementalApplication) application).duration;
		this.appliedAt = ((DurationElementalApplication) application).appliedAt;
	}

	@Override
	public String toString() {
		return String.format(
			"%s@%s[type=DURATION, element=%s, gaugeUnits=%s, duration=%.2f]",
			this.getClass().getSimpleName(),
			Integer.toHexString(this.hashCode()),
			this.getElement().toString(),
			this.getGaugeUnits(),
			this.getDuration()
		);
	}
}
