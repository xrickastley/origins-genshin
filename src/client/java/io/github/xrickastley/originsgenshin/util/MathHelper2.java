package io.github.xrickastley.originsgenshin.util;

public class MathHelper2 {
	public static double endOffset(double value, double offset, double start, double end) {
		return Math.max(Math.max(value, start) - (end - offset), 0) / offset;
	}
}
