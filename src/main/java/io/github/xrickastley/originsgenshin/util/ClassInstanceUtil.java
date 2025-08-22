package io.github.xrickastley.originsgenshin.util;

import java.util.function.Function;

import org.jetbrains.annotations.Nullable;

public class ClassInstanceUtil {
	@SuppressWarnings("unchecked")
	public static <T> T cast(Object instance) {
		return (T) instance;
	}

	/**
	 * Casts the provided {@code instance} into an instance of {@code T} if it's an instance of
	 * {@code T}.
	 * 
	 * @param <T> The type of the instance.
	 * @param instance The object to cast into an instance of {@code T}
	 * @param castClass The target class of the cast.
	 * @return {@code instance} as an instance of {@code T}, {@code null} otherwise.
	 */
	@SuppressWarnings("unchecked")
	public static <T> @Nullable T castOrNull(Object instance, Class<T> castClass) {
		return castClass.isInstance(instance)
			? (T) instance
			: null;
	}

	/**
	 * Maps the provided instance into {@code R} if it isn't {@code null}.
	 * 
	 * @param <T> The type of the instance.
	 * @param <R> The type of the result.
	 * @param instance The instance of {@code T} to map if it isn't null.
	 * @param mapper The mapper to apply if {@code instance} isn't null
	 * @return {@code R} if {@code instance} isn't {@code null}, {@code null} otherwise.
	 */
	public static <T, R> @Nullable R mapOrNull(@Nullable T instance, Function<T, R> mapper) {
		return instance == null
			? null
			: mapper.apply(instance);
	}
}
