package io.github.xrickastley.originsgenshin.mixin.client;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.effect.StatusEffectInstance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;

import net.minecraft.client.gui.screen.ingame.AbstractInventoryScreen;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.mojang.blaze3d.systems.RenderSystem;

@Mixin(AbstractInventoryScreen.class)
public class AbstractInventoryScreenMixin {
	@Inject(
		method = "drawStatusEffectSprites",
		at = @At("HEAD")
	)
	private void enableBlend(DrawContext context, int x, int height, Iterable<StatusEffectInstance> statusEffects, boolean wide, CallbackInfo ci) {
		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();
		RenderSystem.enableCull();
	}
	
	@Inject(
		method = "drawStatusEffectSprites",
		at = @At("TAIL")
	)
	private void disableBlend(DrawContext context, int x, int height, Iterable<StatusEffectInstance> statusEffects, boolean wide, CallbackInfo ci) {
		RenderSystem.disableBlend();
		RenderSystem.disableCull();
	}
}
