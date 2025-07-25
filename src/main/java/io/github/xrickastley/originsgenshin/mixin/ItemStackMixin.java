package io.github.xrickastley.originsgenshin.mixin;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import io.github.xrickastley.originsgenshin.factory.OriginsGenshinStatusEffects;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

// Prioritized since Frozen **MUST** disable using items.
@Mixin(value = ItemStack.class, priority = Integer.MIN_VALUE)
public class ItemStackMixin {
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
}
