package io.github.xrickastley.originsgenshin.mixin;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import io.github.xrickastley.originsgenshin.factory.OriginsGenshinStatusEffects;
import net.minecraft.block.AbstractBlock.AbstractBlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.block.EntityShapeContext;
import net.minecraft.block.ShapeContext;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;

// Prioritized since Frozen **MUST** disable block placements.
@Mixin(value = AbstractBlockState.class, priority = Integer.MIN_VALUE)
public class AbstractBlockStateMixin {
	@Final
	@Inject(
		method = "getOutlineShape(Lnet/minecraft/world/BlockView;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/ShapeContext;)Lnet/minecraft/util/shape/VoxelShape;",
		at = @At("HEAD"),
		cancellable = true
	)
	private void frozen_PreventBlockPlace(BlockView world, BlockPos pos, ShapeContext context, CallbackInfoReturnable<VoxelShape> cir) {
		if (!(
			context instanceof final EntityShapeContext esc 
			&& esc.getEntity() != null 
			&& esc.getEntity() instanceof final PlayerEntity player
		)) return;

		if (player.hasStatusEffect(OriginsGenshinStatusEffects.FROZEN)) cir.setReturnValue(VoxelShapes.empty());
	}
}
