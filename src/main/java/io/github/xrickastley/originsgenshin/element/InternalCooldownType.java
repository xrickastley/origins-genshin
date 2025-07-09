package io.github.xrickastley.originsgenshin.element;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import io.github.xrickastley.originsgenshin.OriginsGenshin;
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
 */
public final class InternalCooldownType {
	public static final InternalCooldownType NONE = new InternalCooldownType(OriginsGenshin.identifier("none"), 0, 0);
	public static final InternalCooldownType DEFAULT = new InternalCooldownType(OriginsGenshin.identifier("default"), 50, 3);

	private final Identifier id;
	private final int resetInterval;
	private final int gaugeSequence;

	private InternalCooldownType(Identifier id, int resetInterval, int gaugeSequence) {
		this.id = id;
		this.resetInterval = resetInterval;
		this.gaugeSequence = gaugeSequence;
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
		return String.format("InternalCooldown[%s/resetInterval=%d,gaugeSequence=%d]", this.id, this.resetInterval, this.gaugeSequence);
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
