package io.github.xrickastley.originsgenshin.element;

import java.util.UUID;
import java.util.function.Function;

import org.jetbrains.annotations.Nullable;

import io.github.xrickastley.originsgenshin.exception.ElementalApplicationOperationException;
import io.github.xrickastley.originsgenshin.exception.ElementalApplicationOperationException.Operation;

import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.MathHelper;

public final class GaugeUnitElementalApplication extends ElementalApplication {
	protected double decayRate;

	protected GaugeUnitElementalApplication(LivingEntity entity, Element element, UUID uuid, double gaugeUnits, boolean aura) {
		super(Type.GAUGE_UNIT, entity, element, uuid, gaugeUnits, true);

		this.decayRate = GaugeUnitElementalApplication.getDefaultDecayRate(gaugeUnits);

		// Aura tax.
		if (this.isAura && element.hasAuraTax()) this.currentGauge *= 0.8;
	}

	/**
	 * Gets the default decay rate per tick, derived from <a href="https://genshin-impact.fandom.com/wiki/Elemental_Gauge_Theory#Aura_Duration_and_Decay_Rate">
	 * Elemental Gauge Theory: Aura Duration and Decay Rate</a>.
	 */
	private static double getDefaultDecayRate(final double gaugeUnits) {
		// Currently in s/GU
		double decayRate = (35 / (4 * gaugeUnits)) + (25.0 / 8.0);
		// Now in ticks/GU
		double decayRateTicks = decayRate / 0.05;
		// Now a GU/tick, allowing us to tick down the gauge easily.
		return 1 / decayRateTicks;
	}

	protected static ElementalApplication fromNbt(LivingEntity entity, NbtCompound nbt, long syncedAt) {
		final Element element = Element.valueOf(nbt.getString("Element"));
		final UUID uuid = nbt.getUuid("UUID");
		final double gaugeUnits = nbt.getDouble("GaugeUnits");
		final double currentGauge = nbt.getDouble("CurrentGauge");
		final boolean isAura = nbt.getBoolean("IsAura");

		final var application = new GaugeUnitElementalApplication(entity, element, uuid, gaugeUnits, isAura);

		final double syncedGaugeDeduction = Math.max(entity.getWorld().getTime() - syncedAt, 0) * application.getDecayRate();
		application.currentGauge = MathHelper.clamp(currentGauge - syncedGaugeDeduction, 0, application.gaugeUnits);

		return application;
	}

	/**
	 * Gets the current decay rate in {@code Gauge Units/tick}.
	 */
	protected double getDecayRate() {
		final @Nullable Function<ElementalApplication, Number> customDecayRate = this.element.getCustomDecayRate();

		return customDecayRate == null
			? this.getDefaultDecayRate()
			: customDecayRate.apply(this).doubleValue();
	}

	@Override
	protected double getDefaultDecayRate() {
		return decayRate;
	}

	@Override
	public int getRemainingTicks() {
		// GU/tick -> ticks/GU
		final double decayRate = 1 / this.getDecayRate();

		return (int) (decayRate * this.currentGauge);
	}

	@Override
	public boolean isEmpty() {
		return this.currentGauge <= 0;
	}

	@Override
	public void tick() {
		super.tick();

		this.currentGauge -= this.getDecayRate();
	}

	@Override
	public void reapply(ElementalApplication application) {
		if (application.element != this.element) throw new ElementalApplicationOperationException(Operation.REAPPLICATION_INVALID_ELEMENT, this, application);

		if (application.type != this.type || !(application instanceof final GaugeUnitElementalApplication guApp)) throw new ElementalApplicationOperationException(Operation.REAPPLICATION_INVALID_TYPES, this, application);

		// The current gauge, handled by currentGauge, is always the most of both applications.
		this.gaugeUnits = Math.max(this.gaugeUnits, guApp.gaugeUnits);
		this.currentGauge = gaugeUnits;

		// The decay rate, handled by gaugeUnits, is always the lesser of both applications, given that the element has Decay Inheritance.
		this.decayRate = this.element.hasDecayInheritance()
			? Math.min(this.decayRate, guApp.decayRate)
			: guApp.decayRate;
	}

	@Override
	public ElementalApplication asAura() {
		return new GaugeUnitElementalApplication(entity, element, UUID.randomUUID(), gaugeUnits, true);
	}

	@Override
	public ElementalApplication asNonAura() {
		return new GaugeUnitElementalApplication(entity, element, UUID.randomUUID(), gaugeUnits, false);
	}

	@Override
	public NbtCompound asNbt()	{
		final NbtCompound nbt = new NbtCompound();

		nbt.putString("Type", this.type.toString());
		nbt.putString("Element", this.element.toString());
		nbt.putUuid("UUID", uuid);
		nbt.putBoolean("IsAura", this.isAura);
		nbt.putDouble("GaugeUnits", this.gaugeUnits);
		nbt.putDouble("CurrentGauge", this.currentGauge);

		return nbt;
	}

	@Override
	public String toString() {
		return String.format(
			"%s@%s[type=GAUGE_UNIT, element=%s, gaugeUnits=%2f, currentGauge=%.2f]",
			this.getClass().getSimpleName(),
			Integer.toHexString(this.hashCode()),
			this.getElement().toString(),
			this.getGaugeUnits(),
			this.getCurrentGauge()
		);
	}
}
