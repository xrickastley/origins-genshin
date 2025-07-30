package io.github.xrickastley.originsgenshin.util;

import org.jetbrains.annotations.Nullable;

public class ClassInstanceUtil {
	@SuppressWarnings("unchecked")
	public static <T> T castInstance(Object instance) {
		return (T) instance;
	}

	@SuppressWarnings("unchecked")
	public static <T> @Nullable T castOrNull(Object instance, Class<T> castClass) {
		return castClass.isInstance(instance)
			? (T) instance
			: null;
	}
}
