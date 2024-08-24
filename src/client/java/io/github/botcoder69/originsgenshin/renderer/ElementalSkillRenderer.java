package io.github.botcoder69.originsgenshin.renderer;

import java.util.ArrayList;

import org.joml.Matrix4f;

import com.mojang.blaze3d.systems.RenderSystem;

import io.github.apace100.apoli.power.ActiveCooldownPower;
import io.github.apace100.apoli.power.PowerType;

import io.github.botcoder69.originsgenshin.OriginsGenshin;
import io.github.botcoder69.originsgenshin.data.ChargeRender;
import io.github.botcoder69.originsgenshin.data.ElementalSkill;
import io.github.botcoder69.originsgenshin.data.ElementalSkillIcon;
import io.github.botcoder69.originsgenshin.interfaces.IActiveCooldownPower;
import io.github.botcoder69.originsgenshin.util.CircleRenderer;
import io.github.botcoder69.originsgenshin.util.ClientConfig;
import io.github.botcoder69.originsgenshin.util.Rescaler;

import me.shedaniel.autoconfig.AutoConfig;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;

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
	 * @return Whether or not the elemental Skill power was replaced.
	 */
	public boolean setOrPersist(ActiveCooldownPower newPower) {
		if (newPower == null || !((IActiveCooldownPower) newPower).hasElementalSkill()) return false;

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
			final ElementalSkill elementalSkillData = ((IActiveCooldownPower) power).getElementalSkill();
			final ElementalSkillIcon skillIcon = elementalSkillData.getRenderedIcon(client.player);

			if (elementalSkillData == null || !elementalSkillData.shouldRender() || skillIcon == null) return;

			final Matrix4f posMatrix = drawContext.getMatrices().peek().getPositionMatrix();
			final CircleRenderer circleRenderer = new CircleRenderer(rescaler.rescaleXWindow(1692), rescaler.rescaleYWindow(992), 0);

			final double percentFilled = resolvePercentFilled(skillIcon, tickDeltaManager);

			RenderSystem.enableBlend();
			RenderSystem.defaultBlendFunc();
			RenderSystem.enableCull();

			this.renderIcon(skillIcon, circleRenderer, drawContext, posMatrix, rescaler, percentFilled);
			this.renderCharges(skillIcon, circleRenderer, drawContext, tickDeltaManager, posMatrix);
			this.renderCooldown(elementalSkillData, skillIcon, drawContext, percentFilled);
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

		// System.out.println(skillIcon.getCharges());

		double newMaximum = pair.getRight() / skillIcon.getCharges();
		
		// pair.getLeft() should be a multiple of newMaximum (that isn't 0) for this to return 0 (no cooldown).
		if ((pair.getLeft() % newMaximum) == 0 && pair.getLeft() > 0) return 0;

		return Math.min(Math.max(0, 1 - (((pair.getLeft() % newMaximum) + tickDeltaManager) / newMaximum)), 1);
	}

	private void renderIcon(ElementalSkillIcon icon, CircleRenderer circleRenderer, DrawContext drawContext, Matrix4f posMatrix, Rescaler rescaler, double percentFilled) {
		final int scaleES = (int) (76.0 * rescaler.getRescaleFactor());
		
		circleRenderer
			.add(38 * rescaler.getRescaleFactorWindow(), 1, 0x64646464)
			.draw(tessellator, posMatrix);

		drawContext.drawTexture(icon.getIcon(), rescaler.rescaleX(1654), rescaler.rescaleY(954), 0, 0, scaleES, scaleES, scaleES, scaleES);
	}

	private void renderCharges(ElementalSkillIcon skillIcon, CircleRenderer circleRenderer, DrawContext drawContext, float tickDeltaManager, Matrix4f posMatrix) {
		final ChargeRender chargeRender = skillIcon.getChargeRender();
		final double percentFilled = resolvePercentFilled(skillIcon, tickDeltaManager);
		final int disable = percentFilled > 0 || skillIcon.getSkill().isDisabled(client.player) || skillIcon.renderAsDisabled(client.player)
			? 1
			: 0;

		if (skillIcon.getChargeRender() == null || skillIcon.getCharges() == 1) {
			circleRenderer
				.add(38 * rescaler.getRescaleFactorWindow(), disable, 0x80646464)
				.add(38 * rescaler.getRescaleFactorWindow(), percentFilled, 0x26c8c8c8)
				.addOutline(34 * rescaler.getRescaleFactorWindow(), 4 * rescaler.getRescaleFactorWindow(), disable, 0x4dc8c8c8)
				.addOutline(34 * rescaler.getRescaleFactorWindow(), 4 * rescaler.getRescaleFactorWindow(), percentFilled, 0x26c8c8c8)
				.draw(tessellator, posMatrix);
		} else {
			final int charges = skillIcon.getCharges();
			final int currentCharges = chargeRender.getCurrentCharges(client.player);

			if (currentCharges == 0) {
				circleRenderer
					.add(38 * rescaler.getRescaleFactorWindow(), disable, 0x80646464)
					.add(38 * rescaler.getRescaleFactorWindow(), percentFilled, 0x26c8c8c8)
					.addOutline(34 * rescaler.getRescaleFactorWindow(), 4 * rescaler.getRescaleFactorWindow(), disable, 0x26c8c8c8)
					.addOutline(34 * rescaler.getRescaleFactorWindow(), 4 * rescaler.getRescaleFactorWindow(), percentFilled, 0x99c8c8c8);
			}

			if (charges > currentCharges) {
				// Render a small cooldown indicator as a circle outline
				circleRenderer
					.addOutline(34 * rescaler.getRescaleFactorWindow(), 4 * rescaler.getRescaleFactorWindow(), percentFilled, 0x99c8c8c8);
			}

			circleRenderer.draw(tessellator, posMatrix);

			final ArrayList<Pair<Integer, Integer>> positions = generateChargesWithCenter(1692, 948, 12, 12, 4, charges);

			for (int i = 0; i < positions.size(); i++) {
				final Pair<Integer, Integer> pos = positions.get(i);
				final Identifier chargeTexture = i < currentCharges
					? OriginsGenshin.identifier("textures/skill/charge.png")
					: OriginsGenshin.identifier("textures/skill/charge_empty.png");

				final int scaleCharge = (int) (12.0 * rescaler.getRescaleFactor());

				drawContext.drawTexture(chargeTexture, rescaler.rescaleX(pos.getLeft()), rescaler.rescaleY(pos.getRight()), 0, 0, scaleCharge, scaleCharge, scaleCharge, scaleCharge);
			}
		}
	}

	private void renderCooldown(ElementalSkill skillData, ElementalSkillIcon skillIcon, DrawContext drawContext, double percentFilled) {
		if (skillIcon.getChargeRender().getMethod() == ChargeRender.Method.CONDITIONAL || !skillData.shouldShowCooldown() || percentFilled == 0) return;

		PowerRenderer.drawCenteredText(
			drawContext,
			client.textRenderer,
			PowerRenderer.changeTextFont(Text.literal(String.format("%.1f", ((double) resolveCooldown(skillIcon) / 20))), OriginsGenshin.identifier("genshin")),
			rescaler.rescaleX(1692),
			rescaler.rescaleY(992),
			0xFFFFFFFF,
			false
		);
	}

	/**
	 * @param x The x-coordinate of the center
	 * @param y The y-coordinate of the center
	 * @param w How wide your texture is.
	 * @param l How high your texture is.
	 * @param s Spacing in-between textures
	 * @param n Number of textures to render.
	 */
	private ArrayList<Pair<Integer, Integer>> generateChargesWithCenter(int x, int y, int w, int l, int s, int n) {
		final ArrayList<Pair<Integer, Integer>> positions = new ArrayList<>();

		/**
		 * final int spacingOffset = Math.max((int) Math.floor((n - 1) / 2) * 2, 0);
		 * -> Math.floor((n - 1) / 2) => For charges of 3 and 4, the first and last charge is brought closer by 1. **Presumably,** for charges 5 and 6, first and last: +2, second-first and second-last: +1
		 * -> ... * 2 => Charges brought closer should always be in pairs.
		 * -> Math.min() => self-explanatory.
		 */
		final int spacingOffset = Math.max((int) Math.floor((n - 1) / 2) * 2, 0);

		/**
		 * final int totalWidth = (w * n) + (s * (n - 1)) - spacingOffset;
		 * -> (w * n) => Total texture length.
		 * -> + (s * (n - 1)) => How many spacing should be inside. This is `(count - 1)` because spacing should be `0` for just one texture.
		 * -> - spacingOffset => How much spacing is "subtracted".
		 */
		final int totalWidth = (w * n) + (s * (n - 1)) - spacingOffset;
		final int offset = totalWidth / 2;

		for (int i = 0; i < n; i++) {
			final int indivOffset = Math.max(Math.max((spacingOffset / 2) - i, (spacingOffset / 2) - ((n - 1) - i)), 0);

			/**
			 * pointX = x - offset + (i * (w + s));
			 * -> (x, y) is the center, not the start, so we subtract `x` by this so we can get the rendering start point, which is (x - offset, y)
			 * -> (i * (w + s)) => The "x-offset" for this texture. `x` should increase by (width + spacing) every time, starting at `1`.
			 * -> - indivOffset => Subtract the individual offset for this texture.
			 */

			positions.add(new Pair<Integer, Integer>(x - offset + (i * (w + s)) - indivOffset, y + (indivOffset * 4)));
		}

		return positions;
	}
}
