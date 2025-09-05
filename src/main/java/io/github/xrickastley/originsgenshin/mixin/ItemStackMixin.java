package io.github.xrickastley.originsgenshin.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;

import java.util.List;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import io.github.xrickastley.originsgenshin.component.ElementalInfusionComponent;
import io.github.xrickastley.originsgenshin.element.Element;
import io.github.xrickastley.originsgenshin.factory.OriginsGenshinStatusEffects;
import io.github.xrickastley.originsgenshin.util.TextHelper;

import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ToolItem;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

// Prioritized since Frozen **MUST** disable using items.
@Mixin(value = ItemStack.class, priority = Integer.MIN_VALUE)
public abstract class ItemStackMixin {
	@Shadow
	public abstract Item getItem();

	@Shadow
	public abstract boolean hasNbt();

	@Final
	@Inject(
		method = "use",
		at = @At("HEAD"),
		cancellable = true
	)
	private void preventItemUse(World world, PlayerEntity user, Hand hand, CallbackInfoReturnable<TypedActionResult<ItemStack>> cir) {
		ItemStack handStack = user.getStackInHand(hand);

		if (user.hasStatusEffect(OriginsGenshinStatusEffects.FROZEN)) cir.setReturnValue(TypedActionResult.fail(handStack));
	}

	@ModifyReturnValue(
		method = "getName",
		at = @At("RETURN")
	)
	private Text modifyName(Text original) {
		if (!(this.getItem() instanceof ToolItem)) return original;

		final ElementalInfusionComponent component = ElementalInfusionComponent.KEY.get(this);

		if (!component.hasElementalInfusion()) return original;

		final Element element = component.getElement();

		return Text.empty()
			.append(original)
			.append(TextHelper.noModifiers(TextHelper.color(" [" + element.getString() + "]", element.getDamageColor())));
	}

	@Inject(
		method = "getTooltip",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/item/ItemStack;isDamaged()Z",
			shift = At.Shift.BEFORE
		)
	)
	private void addInfusionData(@Nullable PlayerEntity player, TooltipContext context, CallbackInfoReturnable<List<Text>> cir, @Local List<Text> texts) {
		if (!(this.getItem() instanceof ToolItem)) return;

		final ElementalInfusionComponent component = ElementalInfusionComponent.KEY.get(this);

		if (!component.hasElementalInfusion()) return;

		final Element element = component.getElement();
		final MutableText message = Text.empty();

		message.append(Text.literal("Elemental Infusion: ").formatted(Formatting.WHITE));
		message.append(TextHelper.color(String.format("%.1fU %s", component.getGaugeUnits(), element.getString()), element.getDamageColor()));

		texts.add(message);
	}
}
