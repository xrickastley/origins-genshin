package io.github.xrickastley.originsgenshin.renderer;

import java.util.Optional;

import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.power.ActiveCooldownPower;
import io.github.apace100.apoli.power.CooldownPower;
import io.github.apace100.apoli.power.Power;
import io.github.apace100.apoli.power.PowerType;
import io.github.apace100.apoli.power.VariableIntPower;

import io.github.xrickastley.originsgenshin.data.RenderableIcon;
import io.github.xrickastley.originsgenshin.util.Rescaler;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.Tessellator;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;

public abstract class PowerRenderer {
	protected final MinecraftClient client = MinecraftClient.getInstance();
	protected final Tessellator tessellator = Tessellator.getInstance();
	protected ActiveCooldownPower power;
	protected final Rescaler rescaler;

	public PowerRenderer(Rescaler rescaler) {
		this(null, rescaler);
	}

	public PowerRenderer(ActiveCooldownPower power, Rescaler rescaler) {
		this.power = power;
		this.rescaler = rescaler;
	}

	public boolean hasPower() {
		return power != null;
	}

	/**
	 * Verifies if this skill with this {@code PowerRenderer} exists on the current client player.
	 */
	public boolean verifySkill() {
		return PowerHolderComponent.KEY.maybeGet(this.client.player)
			.map(component -> component.hasPower(power.getType()))
			.orElse(false);
	}

	/**
	 * Gets the progress of the power in this {@code PowerRenderer}, or the cooldown power inside {@code RenderableIcon} if one exists.
	 * @param icon The {@code RenderableIcon} to get the progress of.
	 * @param tickDeltaManager The {@code tickDeltaManager} given in the {@code HudRenderCallback} event.
	 * @return A double representing the power or cooldown's progress, with {@code 0.0} representing none and {@code 1.0} representing full.
	 */
	public double getProgress(RenderableIcon icon, float tickDeltaManager) {
		if (icon.getCooldown() == null) return 1 - power.getProgress();

		Pair<Integer, Integer> pair = icon.resolveCooldownResource(client.player);

		if (pair.getLeft().equals(pair.getRight())) return Math.min(Math.max(0, 1 - ((double) pair.getLeft() / pair.getRight())), 1);

		return Math.min(Math.max(0, 1 - (((double) pair.getLeft() + tickDeltaManager) / pair.getRight())), 1);
	}
	
	/**
	 * Resolves the cooldown of the provided {@code RenderableIcon} in ticks.
	 * @param icon The icon to resolve cooldown for. 
	 * @return The resulting cooldown, in ticks.
	 */
	protected int resolveCooldown(RenderableIcon icon) {
		final PowerType<?> cooldownPower = icon.getCooldown();

		if (cooldownPower == null) return power.getRemainingTicks();

		Pair<Integer, Integer> pair = icon.resolveCooldownResource(client.player);

		return pair.getRight() - pair.getLeft();
	}

	public static Pair<Integer, Integer> resolveResource(PowerType<?> resource, PlayerEntity player) {
		return resolveResourceAsOptional(resource, player)
			.orElse(null);
	}

	
	public static Optional<Pair<Integer, Integer>> resolveResourceAsOptional(PowerType<?> resource, PlayerEntity player) {
		return PowerRenderer.resolveResourceAsOptional(resource, player, false);
	}

	public static Optional<Pair<Integer, Integer>> resolveResourceAsOptional(PowerType<?> resource, PlayerEntity player, boolean reverse) {
		return PowerHolderComponent.KEY.maybeGet(player)
			.map(component -> {
				final Power power = component.getPower(resource);

				Pair<Integer, Integer> pair = null;

				if (power instanceof VariableIntPower vip) pair = new Pair<Integer, Integer>(vip.getValue(), vip.getMax());
				else if (power instanceof CooldownPower cp) pair = new Pair<Integer, Integer>(cp.getRemainingTicks(), cp.cooldownDuration);

				return reverse
					? pair != null 
						? new Pair<Integer, Integer>(pair.getRight() - pair.getLeft(), pair.getRight())
						: null
					: pair;
			});
	}

	public abstract boolean setOrPersist(ActiveCooldownPower newPower);

	/**
	 * Renders this power in the HUD.
	 * @param context The {@code DrawContext} given in the {@code HudRenderCallback} event.
	 * @param tickDeltaManager The {@code tickDeltaManager} given in the {@code HudRenderCallback} event.
	 */
	public abstract void render(DrawContext context, float tickDeltaManager);
	
	public static int drawCenteredText(DrawContext drawContext, TextRenderer textRenderer, Text text, double x, double y, int color, boolean shadow) {
		return drawContext.drawText(textRenderer, text, (int) (x - (textRenderer.getWidth(text) / 2)), (int) y - (textRenderer.fontHeight / 2), color, shadow);
	}

	public static MutableText changeTextFont(MutableText text, Identifier font) {
		Style style = text.getStyle();
		text.setStyle(style.withFont(font));

		return text;
	}
}
