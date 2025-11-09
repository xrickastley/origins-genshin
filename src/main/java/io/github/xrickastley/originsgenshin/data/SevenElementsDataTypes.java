package io.github.xrickastley.originsgenshin.data;

import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSyntaxException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import io.github.apace100.calio.data.SerializableDataType;
import io.github.apace100.calio.data.SerializableDataTypes;
import io.github.xrickastley.originsgenshin.OriginsGenshin;
import io.github.xrickastley.originsgenshin.integration.SevenElementsIntegration;
import io.github.xrickastley.sevenelements.element.Element;
import io.github.xrickastley.sevenelements.element.ElementalApplication;
import io.github.xrickastley.sevenelements.element.InternalCooldownContext;
import io.github.xrickastley.sevenelements.element.InternalCooldownTag;
import io.github.xrickastley.sevenelements.element.reaction.ElementalReaction;
import io.github.xrickastley.sevenelements.registry.SevenElementsRegistries;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtOps;

public class SevenElementsDataTypes {
	public static final SerializableDataType<Element> ELEMENT
		= SevenElementsDataTypes.enumValue(Element.class);

	public static final SerializableDataType<List<Element>> ELEMENTS
		= SerializableDataType.list(SevenElementsDataTypes.ELEMENT);

	public static final SerializableDataType<ElementalReaction> ELEMENTAL_REACTION
		= SerializableDataType.registry(ElementalReaction.class, SevenElementsRegistries.ELEMENTAL_REACTION);

	public static final SerializableDataType<List<ElementalReaction>> ELEMENTAL_REACTIONS
		= SerializableDataType.list(SevenElementsDataTypes.ELEMENTAL_REACTION);

	public static final SerializableDataType<InternalCooldownTag> INTERNAL_COOLDOWN_TAG
		= SerializableDataType.wrap(InternalCooldownTag.class, SerializableDataTypes.STRING, InternalCooldownTag::getTag, InternalCooldownTag::tag);

	public static final SerializableDataType<ElementalApplication.Type> ELEMENTAL_APPLICATION_TYPE
		= SevenElementsDataTypes.enumValue(ElementalApplication.Type.class);

	public static final SerializableDataType<ElementalApplication.Builder> ELEMENTAL_APPLICATION_BUILDER
		= SevenElementsDataTypes.codec(ElementalApplication.Builder.class, ElementalApplication.Builder.CODEC);

	public static final SerializableDataType<InternalCooldownContext.Builder> INTERNAL_COOLDOWN_CONTEXT_BUILDER
		= SevenElementsDataTypes.codec(InternalCooldownContext.Builder.class, InternalCooldownContext.Builder.CODEC);

	public static <T> SerializableDataType<T> codec(Class<T> dataClass, Codec<T> codec) {
		return new SerializableDataType<>(
			dataClass,
			(buf, inst) -> buf.writeNbt(
				(NbtCompound) codec
					.encodeStart(OriginsGenshin.attemptRegistryWrap(NbtOps.INSTANCE), inst)
					.getOrThrow(false, message -> {})
			),
			buf -> codec
				.parse(OriginsGenshin.attemptRegistryWrap(NbtOps.INSTANCE), buf.readNbt())
				.getOrThrow(false, message -> {}),
			json -> codec
				.parse(OriginsGenshin.attemptRegistryWrap(JsonOps.INSTANCE), json)
				.getOrThrow(false, message -> {})
		);
	}


	public static <T extends Enum<T>> SerializableDataType<T> enumValue(Class<T> dataClass) {
		return new SerializableDataType<>(
			dataClass,
			(buf, inst) -> buf.writeInt(inst.ordinal()),
			buf -> dataClass.getEnumConstants()[buf.readInt()],
			json -> {
				if (!json.isJsonPrimitive())
					throw new JsonSyntaxException("Expected value to be a primitive of either an integer or a string.");

				final JsonPrimitive primitive = json.getAsJsonPrimitive();

				if (primitive.isNumber()) {
					final int enumOrdinal = primitive.getAsInt();
					final T[] enumValues = dataClass.getEnumConstants();

					if (enumOrdinal < 0 || enumOrdinal >= enumValues.length)
						throw new JsonSyntaxException("Expected to be in the range of 0 - " + (enumValues.length - 1));

					return enumValues[enumOrdinal];
				} else if (primitive.isString()) {
                    final String enumName = primitive.getAsString();

				return Stream.of(dataClass.getEnumConstants())
					.filter(e -> e.name().equalsIgnoreCase(enumName))
					.findFirst()
					.orElseThrow(() ->
						new JsonSyntaxException("Expected value to be a case-insensitive string of: " + Stream.of(dataClass.getEnumConstants()).map(e -> e.name()).collect(Collectors.joining(", ")))
					);
				}

				throw new JsonSyntaxException("Expected value to be either an integer or a string.");
			}
		);
	}

	static {
		if (!SevenElementsIntegration.hasSevenElements()) {
			throw new NoClassDefFoundError("io/github/xrickastley/originsgenshin/data/SevenElementsDataTypes; Ensure Seven Elements exists before loading this class!");
		}
	}
}
