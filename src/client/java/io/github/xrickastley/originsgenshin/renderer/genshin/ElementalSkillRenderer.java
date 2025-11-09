package io.github.xrickastley.originsgenshin.renderer.genshin;

import com.mojang.blaze3d.systems.RenderSystem;

import java.util.ArrayList;

import org.joml.Matrix4f;

import io.github.apace100.apoli.power.ActiveCooldownPower;
import io.github.apace100.apoli.power.PowerType;
import io.github.xrickastley.originsgenshin.OriginsGenshin;
import io.github.xrickastley.originsgenshin.data.ChargeRender;
import io.github.xrickastley.originsgenshin.data.ElementalSkill;
import io.github.xrickastley.originsgenshin.data.ElementalSkillIcon;
import io.github.xrickastley.originsgenshin.interfaces.IActiveCooldownPower;
import io.github.xrickastley.originsgenshin.util.CircleRenderer;
import io.github.xrickastley.originsgenshin.util.ClientConfig;
import io.github.xrickastley.originsgenshin.util.Rescaler;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;

import me.shedaniel.autoconfig.AutoConfig;

public class ElementalSkillRenderer extends PowerRenderer {
	public ElementalSkillRenderer(Rescaler rescaler) {
		this(null, rescaler);
	}

	public ElementalSkillRenderer(ActiveCooldownPower power, Rescaler rescaler) {
		super(power, rescaler);
	}

	/**
	 * Resolves the cooldown of the provided {@code ElementalSkillIcon} in ticks, taking charges into account.
	 * @param icon The icon to resolve cooldown for.
	 * @return The resulting cooldown, in ticks.
	 */
	protected int resolveCooldown(ElementalSkillIcon icon) {
		final PowerType<?> cooldownPower = icon.getCooldown();

		if (cooldownPower == null) return power.getRemainingTicks();

		Pair<Integer, Integer> pair = icon.resolveCooldownResource(client.player);

		if (pair.getLeft().equals(pair.getRight())) return 0;

		int newMax = (int) Math.round((double) pair.getRight() / icon.getCharges());

		return newMax - (pair.getLeft() % newMax);
	}

	/**
	 * Compares the currently existing {@code power} and the given {@code newPower}. If {@code newPower} isn't null and {@code newPower.hasElementalSkill()} is true, replaces the current elemental Skill power with the new one.
	 * @param newPower The elemental Skill power to replace the currently existing {@code power}, if it exists.
	 * @return Whether the elemental Skill power was replaced.
	 */
	public boolean setOrPersist(ActiveCooldownPower newPower) {
		if (newPower == null || !((IActiveCooldownPower) newPower).originsgenshin$hasElementalSkill()) return false;

		this.power = newPower;

		return true;
	}

	public void render(DrawContext drawContext, float tickDeltaManager) {
		final ClientConfig config = AutoConfig
			.getConfigHolder(ClientConfig.class)
			.getConfig();

		if (!config.renderers.showElementalSkill || power == null) return;

		if (!verifySkill()) {
			power = null;

			return;
		}

		try {
			final ElementalSkill elementalSkillData = ((IActiveCooldownPower) power).originsgenshin$getElementalSkill();
			final ElementalSkillIcon skillIcon = elementalSkillData.getRenderedIcon(client.player);

			if (elementalSkillData == null || !elementalSkillData.shouldRender() || skillIcon == null) return;

			final MatrixStack matrices = drawContext.getMatrices();
			final CircleRenderer circleRenderer = new CircleRenderer(0, 0, 0);

			final double percentFilled = resolvePercentFilled(skillIcon, tickDeltaManager);

			RenderSystem.enableBlend();
			RenderSystem.defaultBlendFunc();
			RenderSystem.enableCull();

			matrices.push();
			matrices.translate(rescaler.rescaleX(1700), rescaler.rescaleY(992), 0);
			matrices.scale(1, 1, 1);

			final Matrix4f posMatrix = matrices.peek().getPositionMatrix();

			this.renderIcon(skillIcon, circleRenderer, drawContext, posMatrix, rescaler, percentFilled);
			this.renderCharges(skillIcon, circleRenderer, drawContext, tickDeltaManager, posMatrix);
			this.renderCooldown(elementalSkillData, skillIcon, drawContext, percentFilled);

			matrices.pop();
		} catch (Exception e) {
			OriginsGenshin
				.sublogger(ElementalSkillRenderer.class)
				.error("An error occured while trying to render Elemental Skill", e);
		}
	}



	/**
	 * Resolves the "percent filled" of an {@code ElementalSkillIcon}, taking charges into account.
	 * @param skillIcon The {@code ElementalSkillIcon} to resolve the percent filled value for.
	 * @param tickDeltaManager The {@code tickDeltaManager} given in the {@code HudRenderCallback} event.
	 * @return A double representing the icon's "percent filled" value, with {@code 0.0} representing none and {@code 1.0} representing full.
	 */
	private double resolvePercentFilled(ElementalSkillIcon skillIcon, float tickDeltaManager) {
		Pair<Integer, Integer> pair = skillIcon.resolveCooldownResource(client.player);

		double newMaximum = pair.getRight() / skillIcon.getCharges();

		// pair.getLeft() should be a multiple of newMaximum (that isn't 0) for this to return 0 (no cooldown).
		if ((pair.getLeft() % newMaximum) == 0 && pair.getLeft() > 0) return 0;

		return Math.min(Math.max(0, 1 - (((pair.getLeft() % newMaximum) + tickDeltaManager) / newMaximum)), 1);
	}

	private void renderIcon(ElementalSkillIcon icon, CircleRenderer circleRenderer, DrawContext drawContext, Matrix4f posMatrix, Rescaler rescaler, double percentFilled) {
		final int scaleES = (int) (76.0 * rescaler.getRescaleFactor());
		final boolean disable = (percentFilled > 0 && icon.getChargeRender().getCurrentCharges(client.player) == 0)
			|| icon.getSkill().isDisabled(client.player)
			|| icon.renderAsDisabled(client.player);

		circleRenderer
			.add(36 * rescaler.getRescaleFactorWindow(), 1, 0x64646464)
			.draw(posMatrix);

		if (disable) RenderSystem.setShaderColor(1, 1, 1, 0.375f);

		drawContext.drawTexture(icon.getIcon(), -scaleES / 2, -scaleES / 2, 0, 0, scaleES, scaleES, scaleES, scaleES);

		RenderSystem.setShaderColor(1, 1, 1, 1);
	}

	private void renderCharges(ElementalSkillIcon skillIcon, CircleRenderer circleRenderer, DrawContext drawContext, float tickDeltaManager, Matrix4f posMatrix) {
		final ChargeRender chargeRender = skillIcon.getChargeRender();
		final double percentFilled = resolvePercentFilled(skillIcon, tickDeltaManager);
		final int disable = percentFilled > 0 || skillIcon.getSkill().isDisabled(client.player) || skillIcon.renderAsDisabled(client.player)
			? 1
			: 0;

		if (skillIcon.getChargeRender() == null || skillIcon.getCharges() == 1) {
			circleRenderer
				.add(36 * rescaler.getRescaleFactorWindow(), percentFilled, 0x0dc8c8c8)
				.addOutline(32 * rescaler.getRescaleFactorWindow(), 4 * rescaler.getRescaleFactorWindow(), disable, 0x1ac8c8c8)
				.addOutline(32 * rescaler.getRescaleFactorWindow(), 4 * rescaler.getRescaleFactorWindow(), percentFilled, 0x80c8c8c8)
				.draw(posMatrix);
		} else {
			final int charges = skillIcon.getCharges();
			final int currentCharges = chargeRender.getCurrentCharges(client.player);

			if (currentCharges == 0) {
				circleRenderer
					.add(36 * rescaler.getRescaleFactorWindow(), percentFilled, 0x0dc8c8c8)
					.addOutline(32 * rescaler.getRescaleFactorWindow(), 4 * rescaler.getRescaleFactorWindow(), disable, 0x1ac8c8c8)
					.addOutline(32 * rescaler.getRescaleFactorWindow(), 4 * rescaler.getRescaleFactorWindow(), percentFilled, 0x80c8c8c8);
			}

			if (charges > currentCharges) {
				// Render a small cooldown indicator as a circle outline
				circleRenderer
					.addOutline(32 * rescaler.getRescaleFactorWindow(), 4 * rescaler.getRescaleFactorWindow(), percentFilled, 0x99c8c8c8);
			}

			circleRenderer.draw(posMatrix);

			final int scaleCharge = (int) (12.0 * rescaler.getRescaleFactor());
			final ArrayList<Pair<Double, Double>> positions = generateChargesWithCenter(0, (int) (-44 * rescaler.getRescaleFactor()), scaleCharge, scaleCharge, (int) (4 * rescaler.getRescaleFactor()), charges);

			for (int i = 0; i < positions.size(); i++) {
				final Pair<Double, Double> pos = positions.get(i);
				final Identifier chargeTexture = i < currentCharges
					? OriginsGenshin.identifier("textures/skill/charge.png")
					: OriginsGenshin.identifier("textures/skill/charge_empty.png");

				drawContext.drawTexture(chargeTexture, pos.getLeft().intValue(), pos.getRight().intValue(), 0, 0, scaleCharge, scaleCharge, scaleCharge, scaleCharge);
			}
		}
	}

	private void renderCooldown(ElementalSkill skillData, ElementalSkillIcon skillIcon, DrawContext drawContext, double percentFilled) {
		if (skillIcon.getChargeRender().getMethod() == ChargeRender.Method.CONDITIONAL || !skillData.shouldShowCooldown() || percentFilled == 0) return;

		final MatrixStack matrices = drawContext.getMatrices();
		final float scale = (float) (1f * rescaler.getRescaleFactorWindow());

		matrices.push();
		matrices.scale(scale, scale, 1F);

		PowerRenderer.drawCenteredText(
			drawContext,
			client.textRenderer,
			PowerRenderer.changeTextFont(Text.literal(String.format("%.1f", ((double) resolveCooldown(skillIcon) / 20))), OriginsGenshin.identifier("genshin")),
			0,
			0,
			0xFFFFFFFF,
			false
		);

		matrices.pop();
	}

	/**
	 * @param x The x-coordinate of the center
	 * @param y The y-coordinate of the center
	 * @param w How wide your texture is.
	 * @param l How high your texture is.
	 * @param s Spacing in-between textures
	 * @param n Number of textures to render.
	 */
	private ArrayList<Pair<Double, Double>> generateChargesWithCenter(int x, int y, int w, int l, int s, int n) {
		final ArrayList<Pair<Double, Double>> positions = new ArrayList<>();

		final double totalSpacing = (-Math.abs(n - 2) + 4) * (n - 1) / 2;
		final double totalWidth = (w * n) + totalSpacing;
		final double offset = totalWidth / 2;
		final double highestYOffset = s * Math.abs(Math.min(Math.floor(n / 2), n - (Math.floor(n / 2) + 1)) - (Math.ceil(n / 2) - 1));

		for (int i = 0; i < n; i++) {
			final double j = Math.ceil(n / 2) - 1;
			final double yOffset = s * Math.abs(Math.min(i, n - (i + 1)) - j);
			final double spacing = i * (totalSpacing / (n - 1));

			final Pair<Double, Double> pair = new Pair<Double, Double>((x - offset) + (spacing + (i * w)), y + (highestYOffset - yOffset));

			positions.add(pair);
		}

		return positions;
	}
}
