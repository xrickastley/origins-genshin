package io.github.xrickastley.originsgenshin.util;

import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class TextHelper {
	public static MutableText withFont(String text, Identifier font) {
		return withFont(Text.literal(text), font);
	}

	public static MutableText withFont(MutableText text, Identifier font) {
		Style style = text.getStyle();
		text.setStyle(style.withFont(font));

		return text;
	}
}
