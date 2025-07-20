package io.github.xrickastley.originsgenshin.element;

import java.util.ArrayList;
import java.util.List;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import io.github.xrickastley.originsgenshin.OriginsGenshin;
import io.github.xrickastley.originsgenshin.registry.OriginsGenshinRegistries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.minecraft.util.dynamic.Codecs;

/**
 * An {@code InternalCooldownType} is a class used for holding different types of Internal Cooldowns
 * by having various instances of the "Reset Interval" and the "Gauge Sequence" variables. <br> <br>
 * 
 * <h2>Parameters</h2> 
 * 
 * The <b>id</b> is the unique {@code Identifier} that seperates this {@code InternalCooldownType} instance
 * from the rest. Regardless of the <b>reset interval</b> or <b>gauge sequence</b>'s values; if the value
 * for the {@code id} is different, that {@code InternalCooldownType} instance is regarded to be different
 * from one that has the same values. <br> <br>
 * 
 * The <b>reset interval</b> is the amount of ticks required before the timer resets. After the timer
 * has reset, the <b>gauge sequence</b> is set back to {@code 0} and the Internal Cooldown is considered to
 * <b>not</b> be active. <br> <br>
 * 
 * The <b>gauge sequence</b> is the amount of elemental attacks required before an element can be 
 * applied again, where the elemental attacks share ICD. When the amount of elemental attacks 
 * exceed that of the gauge sequence, the Internal Cooldown is considered to <b>not</b> be active,
 * <b>however</b>, this does <b>not</b> reset the timer given by the <b>reset interval</b>. 
 * 
 * <h2>Hardcoded ICD Types</h2> 
 * 
 * An {@code InternalCooldownType} isn't validated or registered against the registry, so a "hardcoded"
 * {@code InternalCooldownType} instance may exist. However, due to the fact that these instances aren't
 * registered against the registry, there may be instances where two seperate {@code InternalCooldownType}
 * instances have the same {@code id}. <br> <br>
 * 
 * Though harmless in practice, it may be confusing to have two {@code InternalCooldownType} instances at
 * the same time. The {@link InternalCooldownType} class provides two methods you may use to create 
 * hardcoded instances of it.
 * 
 * <ul>
 * 	<li>{@link InternalCooldownType#of(Identifier, int, int) InternalCooldownType.of()} creates a <i>purely</i> hardcoded instance of {@code InternalCooldownType}.</li>
 * 	<li>{@link InternalCooldownType#registered(Identifier, int, int) InternalCooldownType.registered()} creates a hardcoded instance of {@code InternalCooldownType} that is 
 * 	registered to the {@link OriginsGenshinRegistries#INTERNAL_COOLDOWN_TYPE} registry. This hardcoded instance is <i>not</i>
 * 	overwritable by data-driven means.</li>
 * </ul>
 */
public final class InternalCooldownType {
	private static final List<InternalCooldownType> PRELOADED_INSTANCES = new ArrayList<>();
	public static final InternalCooldownType NONE = InternalCooldownType.registered(OriginsGenshin.identifier("none"), 0, 0);
	public static final InternalCooldownType DEFAULT = InternalCooldownType.registered(OriginsGenshin.identifier("default"), 50, 3);
	public static final InternalCooldownType INTERVAL_ONLY = InternalCooldownType.registered(OriginsGenshin.identifier("interval_only"), 50, Integer.MAX_VALUE);

	private final Identifier id;
	private final int resetInterval;
	private final int gaugeSequence;

	private InternalCooldownType(Identifier id, int resetInterval, int gaugeSequence) {
		this.id = id;
		this.resetInterval = resetInterval;
		this.gaugeSequence = gaugeSequence;
	}

	public static InternalCooldownType of(Identifier id, int resetInterval, int gaugeSequence) {
		return new InternalCooldownType(id, resetInterval, gaugeSequence);
	}

	public static InternalCooldownType registered(Identifier id, int resetInterval, int gaugeSequence) {
		final InternalCooldownType icdType = new InternalCooldownType(id, resetInterval, gaugeSequence);

		InternalCooldownType.PRELOADED_INSTANCES.add(icdType);

		return icdType;
	}

	public Identifier getId() {
		return id;
	}

	public int getResetInterval() {
		return resetInterval;
	}

	public int getGaugeSequence() {
		return gaugeSequence;
	}

	public Builder getBuilder() {
		return new Builder(resetInterval, gaugeSequence);
	}

	@Override
	public String toString() {
		return String.format("InternalCooldownType[%s/resetInterval=%d,gaugeSequence=%d]", this.id, this.resetInterval, this.gaugeSequence);
	}

	public static void onBeforeRegistryLoad(Registry<InternalCooldownType> registry) {
		InternalCooldownType.PRELOADED_INSTANCES.forEach(inst -> Registry.register(registry, inst.getId(), inst));
	}

	public static final class Builder {
		public static final Codec<InternalCooldownType.Builder> CODEC = RecordCodecBuilder.create(instance ->
			instance
				.group(
					Codecs
						.createStrictOptionalFieldCodec(Codec.intRange(0, Integer.MAX_VALUE), "reset_interval", 50)
						.forGetter(Builder::getResetInterval),
					Codecs
						.createStrictOptionalFieldCodec(Codec.intRange(0, Integer.MAX_VALUE), "gauge_sequence", 3)
						.forGetter(Builder::getGaugeSequence)
				)
				.apply(instance, Builder::new)
		);

		private final int resetInterval;
		private final int gaugeSequence;

		private Builder(int resetInterval, int gaugeSequence) {
			this.resetInterval = resetInterval;
			this.gaugeSequence = gaugeSequence;
		}

		public InternalCooldownType getInstance(Identifier registryId) {
			return new InternalCooldownType(registryId, resetInterval, gaugeSequence);
		}

		private int getResetInterval() {
			return resetInterval;
		}

		private int getGaugeSequence() {
			return gaugeSequence;
		}
	}
}
