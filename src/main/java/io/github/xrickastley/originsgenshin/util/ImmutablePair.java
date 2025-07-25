package io.github.xrickastley.originsgenshin.util;

import net.minecraft.util.Pair;

public class ImmutablePair<A, B> extends Pair<A, B> {
    public ImmutablePair(final A left, final B right) {
        super(left, right);
    }

	public static <A, B> Pair<A, B> of(final Pair<A, B> pair) {
		return new ImmutablePair<A,B>(pair.getLeft(), pair.getRight());
	}

	@Override
	public void setLeft(A left) {
		throw new IllegalStateException("This Pair is immutable!");
	}

	@Override
	public void setRight(B right) {
		throw new IllegalStateException("This Pair is immutable!");
	}
}
