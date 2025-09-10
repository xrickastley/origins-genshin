package io.github.xrickastley.originsgenshin.mixin.client;

import com.llamalad7.mixinextras.sugar.Local;

import java.util.HashSet;
import java.util.Set;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import io.github.xrickastley.originsgenshin.component.ElementComponent;
import io.github.xrickastley.originsgenshin.element.Element;
import io.github.xrickastley.originsgenshin.element.ElementalApplication;
import io.github.xrickastley.originsgenshin.util.Array;
import io.github.xrickastley.originsgenshin.util.CircleRenderer;
import io.github.xrickastley.originsgenshin.util.Functions;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;

@Mixin(InGameHud.class)
public class InGameHudMixin {
	@Shadow
	@Final
	private MinecraftClient client;

	@Inject(
		method = "renderStatusBars",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/util/profiler/Profiler;swap(Ljava/lang/String;)V",
			shift = At.Shift.BEFORE,
			ordinal = 0
		)
	)
	private void renderAppliedElements(DrawContext context, CallbackInfo ci, @Local PlayerEntity player, @Local(ordinal = 3) int x, @Local(ordinal = 9) int y) {
		this.client.getProfiler().swap("origins-genshin:elements");

		int offset = 0;

		if (player.getArmor() > 0) offset += 1;

		y -= (10 * (offset));

		final double scaleFactor = MinecraftClient.getInstance().getWindow().getScaleFactor();
		final Set<Identifier> existing = new HashSet<>();
		final Array<Identifier> appliedElements = ElementComponent.KEY
			.get(player)
			.getAppliedElements()
			.map(Functions.compose(ElementalApplication::getElement, Element::getTexture))
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
