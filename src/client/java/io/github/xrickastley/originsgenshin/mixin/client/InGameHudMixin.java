package io.github.xrickastley.originsgenshin.mixin.client;

import com.llamalad7.mixinextras.sugar.Local;

import java.util.HashSet;
import java.util.Set;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import io.github.xrickastley.originsgenshin.component.ElementComponent;
import io.github.xrickastley.originsgenshin.util.Array;
import io.github.xrickastley.originsgenshin.util.CircleRenderer;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;

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
		int offset = 0;

		if (player.getArmor() > 0) offset += 1;

		offset += Math.ceil((player.getMaxHealth() + player.getAbsorptionAmount()) / 20);

		y -= (10 * (offset));

		final double scaleFactor = MinecraftClient.getInstance().getWindow().getScaleFactor();
		final Set<Identifier> existing = new HashSet<>();
		final Array<Identifier> appliedElements = ElementComponent.KEY
			.get(player)
			.getAppliedElements()
			.map(a -> a.getElement().getTexture())
			.filter(existing::add);

		for (int i = 0; i < appliedElements.length(); i++) {
			final Identifier texture = appliedElements.get(i);
			final int x1 = x + (i * 10);
			final CircleRenderer circleRenderer = new CircleRenderer((x1 + 4.5) * scaleFactor, (y + 4.5) * scaleFactor, 0);

			circleRenderer
				.add(4.5 * scaleFactor, 1, 0x7F646464)
				.draw(context.getMatrices().peek().getPositionMatrix());

			context.drawTexture(texture, x1, y, 9, 9, 0, 0, 9, 9, 9, 9);
		}
	}
}
