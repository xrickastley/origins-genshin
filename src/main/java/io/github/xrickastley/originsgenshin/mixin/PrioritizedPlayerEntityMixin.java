package io.github.xrickastley.originsgenshin.mixin;

import com.mojang.authlib.GameProfile;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import io.github.xrickastley.originsgenshin.factory.OriginsGenshinStatusEffects;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

// Prioritized since Frozen **MUST** disable movements and actions.
@Mixin(value = PlayerEntity.class, priority = Integer.MIN_VALUE)
public abstract class PrioritizedPlayerEntityMixin extends LivingEntity {
	public PrioritizedPlayerEntityMixin(final World world, final BlockPos pos, final float yaw, final GameProfile gameProfile) {
		super(EntityType.PLAYER, world);

		throw new AssertionError();
	}

	@Inject(
		method = "isBlockBreakingRestricted",
		at = @At("HEAD"),
		cancellable = true,
		order = Integer.MAX_VALUE
	)
	private void frozenPreventsBreakingBlocks(CallbackInfoReturnable<Boolean> info) {
		if (this.hasStatusEffect(OriginsGenshinStatusEffects.FROZEN)) info.setReturnValue(true);
	};
}
