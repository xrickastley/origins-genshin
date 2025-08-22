package io.github.xrickastley.originsgenshin.util;

import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class TextHelper {
	public static MutableText font(String text, Identifier font) {
		return font(Text.literal(text), font);
	}

	public static MutableText font(MutableText text, Identifier font) {
		return text.setStyle(text.getStyle().withFont(font));
	}

	public static MutableText gradient(String text, int start, int end) {
		final MutableText result = Text.empty();

	    final int startR = (start >>> 16) & 0xFF;
	    final int startG = (start >>> 8) & 0xFF;
	    final int startB = start & 0xFF;

	    final int endR = (end >>> 16) & 0xFF;
	    final int endG = (end >>> 8) & 0xFF;
	    final int endB = end & 0xFF;

	    for (int i = 0; i < text.length(); i++) {
	        final double step = i / ((double) text.length() - 1);

	        final int r = (int) Math.round(startR + (endR - startR) * step);
	        final int g = (int) Math.round(startG + (endG - startG) * step);
	        final int b = (int) Math.round(startB + (endB - startB) * step);

	        final int color = (r << 16) | (g << 8) | b;

			result.append(
				Text.literal(String.valueOf(text.charAt(i)))
					.fillStyle(Style.EMPTY.withColor(color))
			);
	    }

	    return result;
	}
}
