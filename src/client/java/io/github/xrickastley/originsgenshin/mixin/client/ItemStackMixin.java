package io.github.xrickastley.originsgenshin.mixin.client;

import java.util.List;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.llamalad7.mixinextras.sugar.Local;

import io.github.xrickastley.originsgenshin.component.ElementalInfusionComponent;
import io.github.xrickastley.originsgenshin.element.Element;
import io.github.xrickastley.originsgenshin.element.InternalCooldownTag;
import io.github.xrickastley.originsgenshin.element.InternalCooldownType;
import io.github.xrickastley.originsgenshin.element.InternalCooldownContext.Builder;
import io.github.xrickastley.originsgenshin.util.ClassInstanceUtil;
import io.github.xrickastley.originsgenshin.util.Functions;
import io.github.xrickastley.originsgenshin.util.JavaScriptUtil;
import io.github.xrickastley.originsgenshin.util.TextHelper;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ToolItem;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin {
	@Shadow
	public abstract Item getItem();

	@Inject(
		method = "getTooltip",
		at = @At(
			value = "INVOKE",
			target = "Ljava/util/List;add(Ljava/lang/Object;)Z",
			ordinal = 16
		)
	)
	private void addInfusionData(@Nullable PlayerEntity player, TooltipContext context, CallbackInfoReturnable<List<Text>> cir, @Local List<Text> texts) {
		if (!(this.getItem() instanceof ToolItem)) return;

		final ElementalInfusionComponent component = ElementalInfusionComponent.KEY.get(this);

		if (component == null || !component.hasElementalInfusion()) return;

		final MutableText message = Text.empty();
		final Element element = component.getElement();
		final Builder builder = component.getInternalCooldown();

		message.append(Text.translatable("item.origins-genshin.components.infusion.infusion").formatted(Formatting.WHITE));
		message.append(TextHelper.color(String.format("%.1fU %s", component.getGaugeUnits(), element.getString()), element.getDamageColor()));

		texts.add(message);



		@Nullable String tagString = ClassInstanceUtil.mapOrNull(builder, Functions.compose(Builder::getTag, InternalCooldownTag::getTag));

		final Text tag = tagString != null
			? Text.literal(tagString).formatted(Formatting.DARK_GRAY)
			: Text.literal("none").formatted(Formatting.RED);

		texts.add(
			Text.empty()
				.append(Text.translatable("item.origins-genshin.components.infusion.tag").formatted(Formatting.WHITE))
				.append(tag)
		);



		final InternalCooldownType type = JavaScriptUtil.nullishCoalesing(
			ClassInstanceUtil.mapOrNull(builder, Builder::getType),
			InternalCooldownType.DEFAULT
		);

		final Text typeFormat = Text.empty()
			.append("(")
			.append(Text.translatable("origins-genshin.formats.icd_type", type.getResetInterval() / 20.0, type.getGaugeSequence()))
			.append(")");

		texts.add(
			Text.empty()
				.append(Text.translatable("item.origins-genshin.components.infusion.type").formatted(Formatting.WHITE))
				.append(
					Text.empty()
						.append(type.getId().toString() + " ")
						.append(typeFormat)
						.formatted(Formatting.DARK_GRAY)
				)
		);
	}
}
