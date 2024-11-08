package io.github.xrickastley.originsgenshin.util;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Color {
	protected int red;
	protected int green;
	protected int blue;
	protected float alpha;

	public Color(int red, int green, int blue) {
		this(red, green, blue, 1F);
	}

	public Color(int red, int green, int blue, float alpha) {
		this.alpha = Math.min(1, Math.max(alpha, 0));
		this.red = Math.min(255, Math.max(red, 0));
		this.green = Math.min(255, Math.max(green, 0));
		this.blue = Math.min(255, Math.max(blue, 0));
	}

	public int getRed() {
		return red;
	}

	public int getGreen() {
		return green;
	}

	public int getBlue() {
		return blue;
	}

	public float getAlpha() {
		return alpha;
	}
	
	/**
	 * Gets the {@code red} value as a percent.
	 * @return The {@code red} value as a percent from {@code 0} to {@code 255}, defined as {@code red / 255}.
	 */
	public float getRedAsPercent() {
		return red / 255F;
	}

	/**
	 * Gets the {@code green} value as a percent.
	 * @return The {@code red} value as a percent from {@code 0} to {@code 255}, defined as {@code blue / 255}.
	 */
	public float getGreenAsPercent() {
		return green / 255F;
	}

	/**
	 * Gets the {@code blue} value as a percent.
	 * @return The {@code blue} value as a percent from {@code 0} to {@code 255}, defined as {@code blue / 255}.
	 */
	public float getBlueAsPercent() {
		return blue / 255F;
	}

	/**
	 * Gets the {@code alpha} value as a number.
	 * @return The {@code alpha} value as a number in the range {@code 0} to {@code 255}, defined as {@code alpha * 255}.
	 */
	public int getAlphaAsRange() {
		return (int) alpha * 255;
	}

	public String asHex() {
		return String.format(
			"#%s%s%s%s", 
			Integer.toHexString(red), 
			Integer.toHexString(green), 
			Integer.toHexString(blue), 
			Integer.toHexString((int) (alpha * 255))
		);
	}

	public int asARGB() {
		int a = (int) (alpha * 255) & 0xFF;
		int r = red & 0xFF;
		int g = green & 0xFF;
		int b = blue & 0xFF;
	
		return (a << 24) | (r << 16) | (g << 8) | b;
	}

	/**
	 * Creates a new {@code Color} based on the values of this color.
	 * @return The new {@code Color}
	 */
	public Color from() {
		return new Color(this.red, this.green, this.blue, this.alpha);
	}

	/**
	 * Adds {@code factor} to this {@code Color}.
	 * @param factor The factor to add to this color.
	 * @return This {@code Color} object.
	 */
	public Color add(double factor) {
		return add(factor, factor, factor, factor);
	}
	
	/**
	 * Adds all the values of this {@code Color} by their respective factors.
	 * @param r The factor to add to the red value.
	 * @param g The factor to add to the green value.
	 * @param b The factor to add to the blue value.
	 * @return This {@code Color} object.
	 */
	public Color add(double r, double g, double b) {
		return add(r, g, b, 1);
	}
	
	/**
	 * Adds all the values of this {@code Color} by their respective factors.
	 * @param r The factor to add to the red value.
	 * @param g The factor to add to the green value.
	 * @param b The factor to add to the blue value.
	 * @param a The factor to add to the alpha value.
	 * @return This {@code Color} object.
	 */
	public Color add(double r, double g, double b, double a) {
		this.red += r;
		this.green += g;
		this.blue += b;
		this.alpha += a;

		normalizeColors();

		return this;
	}

	/**
	 * Multiplies this {@code Color} by {@code factor}.
	 * @param factor The factor to multiply this color with.
	 * @return This {@code Color} object.
	 */
	public Color multiply(double factor) {
		return multiply(factor, factor, factor, factor);
	}
	
	/**
	 * Multiplies all the values of this {@code Color} by their respective factors.
	 * @param r The factor to multiply the red value with.
	 * @param g The factor to multiply the green value with.
	 * @param b The factor to multiply the blue value with.
	 * @return This {@code Color} object.
	 */
	public Color multiply(double r, double g, double b) {
		return multiply(r, g, b, 1);
	}
	
	/**
	 * Multiplies all the values of this {@code Color} by their respective factors.
	 * @param r The factor to multiply the red value with.
	 * @param g The factor to multiply the green value with.
	 * @param b The factor to multiply the blue value with.
	 * @param a The factor to multiply the alpha value with.
	 * @return This {@code Color} object.
	 */
	public Color multiply(double r, double g, double b, double a) {
		this.red *= r;
		this.green *= g;
		this.blue *= b;
		this.alpha *= a;

		normalizeColors();

		return this;
	}

	private void normalizeColors() {
		this.alpha = Math.min(1, Math.max(this.alpha, 0));
		this.red = Math.min(255, Math.max(this.red, 0));
		this.green = Math.min(255, Math.max(this.green, 0));
		this.blue = Math.min(255, Math.max(this.blue, 0));
	}

	@Override
	public String toString() {
		return String.format("%s(r=%d, g=%d, b=%d, a=%f)", this.getClass().getSimpleName(), red, green, blue, alpha);
	}

	/**
	 * Turns an RGBA hex string {@code #rrggbbaa} to a {@code Color} object. This method also permits RGB hex strings {@code #rrggbb} and will use {@code 1} as the value for the {@code alpha}.
	 * @param rgbaHex An RGBA hex string {@code #rrggbbaa} or an RGB hex string {@code #rrggbb}
	 * @return A {@code Color} object created from the provided hex string.
	 */
	public static Color fromRGBAHex(String rgbaHex) {
		Pattern pattern = Pattern.compile("[a-f0-9]{2}");
        Matcher matcher = pattern.matcher(rgbaHex.strip().replace("#", "").toLowerCase());
        ArrayList<String> hexCodes = new ArrayList<>();

        while (matcher.find()) hexCodes.add(matcher.group());

		return new Color(
			Integer.parseInt(hexCodes.get(0), 16),
			Integer.parseInt(hexCodes.get(1), 16),
			Integer.parseInt(hexCodes.get(2), 16),
			hexCodes.size() >= 4
				? Integer.parseInt(hexCodes.get(3), 16) / 255
				: 1
		);
	}
	
	public static Color fromARGBHex(int argbHex) {
		int alpha = (argbHex >> 24) & 0xFF;
		int red = (argbHex >> 16) & 0xFF;
		int green = (argbHex >> 8) & 0xFF;
		int blue = argbHex & 0xFF;
		float a = alpha / 255f;

		return new Color(red, green, blue, a);
	}
}
