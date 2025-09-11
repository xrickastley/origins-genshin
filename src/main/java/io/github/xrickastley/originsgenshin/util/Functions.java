package io.github.xrickastley.originsgenshin.util;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class Functions {
	public static <A, B, R> Predicate<A> composePredicate(Function<A, B> fn1, Predicate<B> fn2) {
		return a -> fn2.test(fn1.apply(a));
	}

	public static <A, B, C, R> Predicate<A> composePredicate(Function<A, B> fn1, Function<B, C> fn2, Predicate<C> fn3) {
		return a -> fn3.test(fn2.compose(fn1).apply(a));
	}

	public static <A, B, C, D, R> Predicate<A> composePredicate(Function<A, B> fn1, Function<B, C> fn2, Function<C, D> fn3, Predicate<D> fn4) {
		return a -> fn4.test(fn3.compose(fn2.compose(fn1)).apply(a));
	}


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

	public static <T> Predicate<T> predicate(Function<T, Boolean> fn) {
		return t -> fn.apply(t);
	}

	public static <T, R> Supplier<R> supplier(Function<T, R> fn, T value) {
		return () -> fn.apply(value);
	}
}
