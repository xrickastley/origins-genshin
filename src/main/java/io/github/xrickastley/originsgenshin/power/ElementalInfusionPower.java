package io.github.xrickastley.originsgenshin.power;

import io.github.apace100.apoli.power.Power;
import io.github.apace100.apoli.power.PowerType;
import io.github.apace100.apoli.power.factory.PowerFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import io.github.xrickastley.originsgenshin.OriginsGenshin;
import io.github.xrickastley.originsgenshin.data.OriginsGenshinDataTypes;
import io.github.xrickastley.originsgenshin.element.ElementalApplication;
import io.github.xrickastley.originsgenshin.element.InternalCooldownContext;

import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.util.Util;

public final class ElementalInfusionPower
	extends Power
	implements Comparable<ElementalInfusionPower>
{
	private final ElementalApplication.Builder application;
	private final InternalCooldownContext icdContext;
	private final int priority;
	private long appliedAt;

	private ElementalInfusionPower(PowerType<?> type, LivingEntity entity, ElementalApplication.Builder application, InternalCooldownContext.Builder icdContextBuilder, int priority) {
		super(type, entity);

		this.application = application;
		this.icdContext = icdContextBuilder.build(entity);
		this.priority = priority;
		this.appliedAt = Util.getMeasuringTimeNano();
	}

	public ElementalApplication getApplication(LivingEntity entity) {
		return application.build(entity);
	}

	public InternalCooldownContext getIcdContext() {
		return icdContext;
	}

	public int getPriority() {
		return priority;
	}

	@Override
	public int compareTo(ElementalInfusionPower o) {
		return this.priority != o.priority
			? Integer.compare(this.priority, o.priority)
			: Long.compare(this.appliedAt, o.appliedAt);
	}

	@Override
	public NbtElement toTag() {
		final NbtCompound compound = new NbtCompound();

		compound.putLong("AppliedAt", this.appliedAt);

		return compound;
	}

	@Override
	public void fromTag(NbtElement tag) {
		if (!(tag instanceof final NbtCompound compound)) return;

		this.appliedAt = compound.getLong("AppliedAt");
	}

	public static PowerFactory<?> createFactory() {
		return new PowerFactory<>(
			OriginsGenshin.identifier("elemental_infusion"),
			new SerializableData()
				.add("element", OriginsGenshinDataTypes.ELEMENTAL_APPLICATION_BUILDER)
				.add("internal_cooldown", OriginsGenshinDataTypes.INTERNAL_COOLDOWN_CONTEXT_BUILDER, InternalCooldownContext.Builder.ofNone())
				.add("priority", SerializableDataTypes.INT),
			data -> (type, entity) -> new ElementalInfusionPower(type, entity, data.get("element"), data.get("internal_cooldown"), data.get("priority"))
		).allowCondition();
	}
}
