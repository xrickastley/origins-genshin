package io.github.xrickastley.originsgenshin.util;

import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

public class EnumUtil {
	public static <T extends Enum<T>> Optional<T> valueOf(Class<T> enumClass, String name) {
		return Stream.of(enumClass.getEnumConstants())
			.filter(e -> e.name().equalsIgnoreCase(name))
			.findFirst();
	}

	public static <T extends Enum<T>> Function<String, T> valueOf(Class<T> enumClass) {
		return value -> EnumUtil.valueOf(enumClass, value)
			.orElseThrow(() -> new IllegalArgumentException(" No case-insensitive enum constant " + enumClass.getName() + "." + value));
	}
}
