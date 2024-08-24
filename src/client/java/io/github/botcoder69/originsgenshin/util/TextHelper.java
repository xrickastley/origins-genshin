package io.github.botcoder69.originsgenshin.util;

import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class TextHelper {
	public static MutableText changeTextFont(String text, Identifier font) {
		return changeTextFont(Text.literal(text), font);
	}

	public static MutableText changeTextFont(MutableText text, Identifier font) {
		Style style = text.getStyle();
		text.setStyle(style.withFont(font));

		return text;
	}
}
