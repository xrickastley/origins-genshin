package io.github.xrickastley.originsgenshin.mixin;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import io.github.xrickastley.originsgenshin.factory.OriginsGenshinStatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

// Prioritized since Frozen **MUST** disable actions.
@Mixin(value = BlockItem.class, priority = Integer.MIN_VALUE)
public class BlockItemMixin {
	@Final
	@WrapOperation(
		method = "useOnBlock",
		at = @At(
			value = "INVOKE", 
			target = "Lnet/minecraft/item/BlockItem;use(Lnet/minecraft/world/World;Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/util/Hand;)Lnet/minecraft/util/TypedActionResult;"
		)
	)
	private TypedActionResult<ItemStack> frozen_PreventItemUse(BlockItem instance, World world, PlayerEntity user, Hand hand, Operation<TypedActionResult<ItemStack>> original) {
		ItemStack handStack = user.getStackInHand(hand);

		return user.hasStatusEffect(OriginsGenshinStatusEffects.FROZEN)
			? TypedActionResult.fail(handStack)
			: original.call(instance, world, user, hand);
	}
}
