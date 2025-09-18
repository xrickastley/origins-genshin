package io.github.xrickastley.originsgenshin.util;

import java.util.Iterator;
import java.util.function.Predicate;

public final class FilteredIterator<T> implements Iterator<T> {
	private final Iterator<T> iterator;
	private final Predicate<? super T> predicate;
	private T next;
	private boolean hasNext;
	private boolean computed;
	private boolean canRemove;

	private FilteredIterator(Iterator<T> iterator, Predicate<? super T> predicate) {
		this.iterator = iterator;
		this.predicate = predicate;
	}

	public static <T> FilteredIterator<T> of(Iterator<T> iterator, Predicate<? super T> predicate) {
		return new FilteredIterator<>(iterator, predicate);
	}

	private void computeNext() {
		if (computed) return;

		while (iterator.hasNext()) {
			final T value = iterator.next();

			if (!predicate.test(value)) continue;

			next = value;
			hasNext = true;
			computed = true;

			return;
		}

		next = null;
		hasNext = false;
		computed = true;
	}

	@Override
	public boolean hasNext() {
		computeNext();

		// Bad normally, but works in the case of clearStatusEffects
		this.canRemove = false;

		return hasNext;
	}

	@Override
	public T next() {
		computeNext();

		this.computed = false;
		this.canRemove = true;

		return this.next;
	}

	@Override
	public void remove() {
		if (!this.canRemove) throw new IllegalStateException("remove() can only be called once after next()");

		this.iterator.remove();
		this.canRemove = false;
	}
}
