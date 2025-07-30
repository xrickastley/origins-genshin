package io.github.xrickastley.originsgenshin.mixin.client;

import com.llamalad7.mixinextras.sugar.Local;
import io.github.xrickastley.originsgenshin.component.ElementComponent;
import io.github.xrickastley.originsgenshin.util.Array;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public class InGameHudMixin {
	@Inject(
		method = "renderStatusBars",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/client/gui/hud/InGameHud;renderHealthBar(Lnet/minecraft/client/gui/DrawContext;Lnet/minecraft/entity/player/PlayerEntity;IIIIFIIIZ)V",
			shift = At.Shift.AFTER
		)
	)
	private void renderAppliedElements(DrawContext context, CallbackInfo ci, @Local PlayerEntity player, @Local(ordinal = 3) int x, @Local(ordinal = 5) int y) {
		y -= (10 * 2);

		final Array<Identifier> appliedElements = ElementComponent.KEY
			.get(player)
			.getAppliedElements()
			.map(a -> a.getElement().getTexture());

		for (int i = 0; i < appliedElements.length(); i++) {
			final Identifier texture = appliedElements.get(i);
			final int x1 = x + (i * 10);

			context.drawTexture(texture, x1, y, 9, 9, 0, 0, 9, 9, 9, 9);

			// context.drawGuiTexture(texture, x1, y, 9, 9);
		}
	}
}
