package io.github.xrickastley.originsgenshin.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import io.github.xrickastley.originsgenshin.util.DelayedRenderer;

import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;

@Mixin(GameRenderer.class)
public class GameRendererMixin {
	@Inject(
		method = "renderWorld",
		at = @At(
			value = "INVOKE",
			target = "Lcom/mojang/blaze3d/systems/RenderSystem;clear(IZ)V"
		)
	)
	private void drawText(final float tickDelta, final long limitTime, final MatrixStack matrices, final CallbackInfo ci) {
		DelayedRenderer.render(tickDelta, matrices);
	}
}
