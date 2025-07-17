package io.github.xrickastley.originsgenshin.util;

public class MathHelper2 {
	public static double endOffset(double value, double offset, double start, double end) {
		return Math.max(Math.max(value, start) - (end - offset), 0) / offset;
	}

	/**
	 * Rounds a value to the specified amount of significant figures.
	 * @param value The value to round.
	 * @param sigFigs The amount of significant figures.
	 */
	public static float roundTo(float value, int sigFigs) {
		if (sigFigs <= 0) throw new ArithmeticException("The amount of significant figures provided must be greater than 0!");

		if (value < 1) return (float) (Math.round(value * Math.pow(10, sigFigs)) / Math.pow(10, sigFigs));

		final int power = String.valueOf((int) value).length();
		final double decimal = value / Math.pow(10, power);

		return (float) (Math.round(decimal * Math.pow(10, sigFigs)) / Math.pow(10, sigFigs) * Math.pow(10, power));
	}
}
