package io.github.xrickastley.originsgenshin.util;

import java.util.function.Consumer;
import java.util.function.Function;

public class Functions {
	public static <A, B, R> Function<A, R> compose(Function<A, B> fn1, Function<B, R> fn2) {
		return fn2.compose(fn1);
	}

	public static <A, B, C, R> Function<A, R> compose(Function<A, B> fn1, Function<B, C> fn2, Function<C, R> fn3) {
		return fn3.compose(fn2.compose(fn1));
	}

	public static <A, B, C, D, R> Function<A, R> compose(Function<A, B> fn1, Function<B, C> fn2, Function<C, D> fn3, Function<D, R> fn4) {
		return fn4.compose(fn3.compose(fn2.compose(fn1)));
	}

	public static <T, R> Consumer<T> consumer(Function<T, R> fn) {
		return t -> fn.apply(t);
	}
}
