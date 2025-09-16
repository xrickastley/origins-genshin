package io.github.xrickastley.originsgenshin.element.reaction;

import io.github.xrickastley.originsgenshin.OriginsGenshin;
import io.github.xrickastley.originsgenshin.element.Element;
import io.github.xrickastley.originsgenshin.util.TextHelper;
import net.minecraft.text.Text;

public final class GeoShatterElementalReaction extends ShatterElementalReaction {
	GeoShatterElementalReaction() {
		super(
			new ElementalReactionSettings("Shatter", OriginsGenshin.identifier("shatter_geo"), TextHelper.font(TextHelper.gradient(Text.translatable("reaction.origins-genshin.shatter").getString(), 0xcfffff, 0x70dee4), TextHelper.GENSHIN_FONT))
				.setReactionCoefficient(0)
				.setAuraElement(Element.FREEZE)
				.setTriggeringElement(Element.GEO, 1)
		);
	}
}
