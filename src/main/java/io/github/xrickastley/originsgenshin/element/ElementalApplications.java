package io.github.xrickastley.originsgenshin.element;

import java.util.UUID;

import org.slf4j.Logger;

import io.github.xrickastley.originsgenshin.OriginsGenshin;
import io.github.xrickastley.originsgenshin.element.ElementalApplication.Type;
import io.github.xrickastley.originsgenshin.exception.ElementalApplicationOperationException;
import io.github.xrickastley.originsgenshin.exception.ElementalApplicationOperationException.Operation;

import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;

public class ElementalApplications {
	private static final Logger LOGGER = OriginsGenshin.sublogger(ElementalApplications.class);

	/**
	 * Creates an Elemental Application using Elemental Gauge Units.
	 * @param entity The entity to create an Elemental Application for.
	 * @param element The Element of this Elemental Application.
	 * @param gaugeUnits The amount of Elemental Gauge Units this Elemental Application has.
	 */
	public static ElementalApplication gaugeUnits(LivingEntity entity, Element element, double gaugeUnits) {
		return ElementalApplications.gaugeUnits(entity, element, gaugeUnits, true);
	}

	/**
	 * Creates an Elemental Application using Elemental Gauge Units.
	 * @param entity The entity to create an Elemental Application for.
	 * @param element The Element of this Elemental Application.
	 * @param gaugeUnits The amount of Elemental Gauge Units this Elemental Application has.
	 * @param aura Whether or not this Elemental Application is an Aura Element. This means that the <a href="https://genshin-impact.fandom.com/wiki/Elemental_Gauge_Theory#Aura_Tax">Aura Tax</a> applies to the current gauge units of this Element.
	 */
	public static ElementalApplication gaugeUnits(LivingEntity entity, Element element, double gaugeUnits, boolean aura) {
		return new GaugeUnitElementalApplication(entity, element, UUID.randomUUID(), gaugeUnits, aura);
	}

	/**
	 * Creates an Elemental Application with a specified duration.
	 * @param entity The entity to create an Elemental Application for.
	 * @param element The Element of this Elemental Application.
	 * @param gaugeUnits The amount of Gauge Units this Elemental Application has.
	 * @param duration The duration of the Elemental Application, in ticks.
	 */
	public static ElementalApplication duration(LivingEntity entity, Element element, double gaugeUnits, double duration) {
		return new DurationElementalApplication(entity, element, UUID.randomUUID(), gaugeUnits, duration);
	}

	/**
	 * Creates an Elemental Application from an NBT.
	 * @param entity The entity to create an Elemental Application for.
	 * @param nbt The NBT to create the Elemental Application from.
	 */
	public static ElementalApplication fromNbt(LivingEntity entity, NbtElement nbt, long syncedAt) {
		if (!(nbt instanceof final NbtCompound compound)) throw new ElementalApplicationOperationException(Operation.INVALID_NBT_DATA, null, null);

		final Type type = Type.valueOf(compound.getString("Type"));

		if (entity.getWorld().getTime() < syncedAt) LOGGER.warn("Current world time: {} is lesser than sync time: {}!", entity.getWorld().getTime(), syncedAt);

		return type == Type.GAUGE_UNIT
			? GaugeUnitElementalApplication.fromNbt(entity, compound, syncedAt)
			: DurationElementalApplication.fromNbt(entity, compound, syncedAt);
	}
}
