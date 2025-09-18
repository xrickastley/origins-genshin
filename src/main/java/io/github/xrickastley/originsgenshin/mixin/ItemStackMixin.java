package io.github.xrickastley.originsgenshin.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import io.github.xrickastley.originsgenshin.component.ElementalInfusionComponent;
import io.github.xrickastley.originsgenshin.element.Element;
import io.github.xrickastley.originsgenshin.factory.OriginsGenshinStatusEffects;
import io.github.xrickastley.originsgenshin.util.TextHelper;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ToolItem;
import net.minecraft.text.Text;
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


	@WrapOperation(
		method = "use",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/item/Item;use(Lnet/minecraft/world/World;Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/util/Hand;)Lnet/minecraft/util/TypedActionResult;"
		)
	)
	private TypedActionResult<ItemStack> frozenPreventsItemUse(Item instance, World world, PlayerEntity user, Hand hand, Operation<TypedActionResult<ItemStack>> original) {
		ItemStack handStack = user.getStackInHand(hand);

		return user.hasStatusEffect(OriginsGenshinStatusEffects.FROZEN)
			? TypedActionResult.fail(handStack)
			: original.call(instance, world, user, hand);
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
}
