package io.github.xrickastley.originsgenshin.renderer;

import org.joml.Matrix4f;

import com.mojang.blaze3d.systems.RenderSystem;

import io.github.apace100.apoli.power.ActiveCooldownPower;

import io.github.xrickastley.originsgenshin.OriginsGenshin;
import io.github.xrickastley.originsgenshin.data.ElementalBurst;
import io.github.xrickastley.originsgenshin.data.ElementalBurstIcon;
import io.github.xrickastley.originsgenshin.interfaces.IActiveCooldownPower;
import io.github.xrickastley.originsgenshin.util.CircleRenderer;
import io.github.xrickastley.originsgenshin.util.ClientConfig;
import io.github.xrickastley.originsgenshin.util.Color;
import io.github.xrickastley.originsgenshin.util.Rescaler;

import me.shedaniel.autoconfig.AutoConfig;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;

public class ElementalBurstRenderer extends PowerRenderer {
	public ElementalBurstRenderer(Rescaler rescaler) {
		this(null, rescaler);
	}

	public ElementalBurstRenderer(ActiveCooldownPower power, Rescaler rescaler) {
		super(power, rescaler);
	}

	/**
	 * Compares the currently existing {@code power} and the given {@code newElementalBurst}. If {@code newElementalBurst} isn't null and {@code newElementalBurst.hasElementalBurst()} is true, replaces the current elemental burst power with the new one.
	 * @param newPower The elemental burst power to replace the currently existing {@code power}, if it exists.
	 * @return Whether or not the elemental burst power was replaced.
	 */
	public boolean setOrPersist(ActiveCooldownPower newPower) {
		newPower.toJson().has("elemental_burst");
		if (newPower == null || !((IActiveCooldownPower) newPower).hasElementalBurst()) return false;

		this.power = newPower;

		return true;
	}

	@Override
	public void render(DrawContext drawContext, float tickDeltaManager) {
		final ClientConfig config = AutoConfig
			.getConfigHolder(ClientConfig.class)
			.getConfig();

		if (!config.renderers.showElementalBurst || power == null) return;

		if (!verifySkill()) {
			power = null;

			return;
		}

		try {
			final ElementalBurst elementalBurstData = ((IActiveCooldownPower) power).getElementalBurst();
			final ElementalBurstIcon burstIcon = elementalBurstData.getRenderedIcon(client.player);

			if (elementalBurstData == null || !elementalBurstData.shouldRender() || burstIcon == null) return;
			
			final Matrix4f posMatrix = drawContext.getMatrices().peek().getPositionMatrix();
			final CircleRenderer circleRenderer = new CircleRenderer(rescaler.rescaleXWindow(1819), rescaler.rescaleYWindow(972), 0);

			final double percentFilled = this.getProgress(burstIcon, tickDeltaManager);

			// System.out.println(percentFilled);

			RenderSystem.enableBlend();
			RenderSystem.defaultBlendFunc();
			RenderSystem.enableCull();

			this.renderFill(burstIcon, circleRenderer, drawContext, posMatrix);
			this.renderIcon(elementalBurstData, burstIcon, circleRenderer, drawContext, posMatrix, rescaler, percentFilled);
			this.renderCooldown(elementalBurstData, burstIcon, drawContext, posMatrix, rescaler, percentFilled);
		} catch (Exception e) {
			OriginsGenshin
				.sublogger(ElementalBurstRenderer.class)
				.error("An error occured while trying to render Elemental Burst", e);
		}
	}

	private void renderFill(ElementalBurstIcon icon, CircleRenderer circleRenderer, DrawContext drawContext, Matrix4f posMatrix) {
		circleRenderer
			.add(56 * rescaler.getRescaleFactorWindow(), 1, 0x64646464)
			.draw(tessellator, posMatrix);
		
		if (icon.getColor() == null) return;
		
		final double resourceMultiplier = this.resolveFillResource(icon);

		if (resourceMultiplier == -1) return;

		drawContext.enableScissor(
			rescaler.rescaleX(1763),
			rescaler.rescaleY(1029 - (56 * 2) * resourceMultiplier),
			rescaler.rescaleX(1763 + (56 * 2)),
			rescaler.rescaleY(1029)
		);

		circleRenderer
			.add(56 * rescaler.getRescaleFactorWindow(), 1, icon.getColor().asARGB());

		if (resourceMultiplier == 1 && icon.getOutlineColor() != null) {
			circleRenderer
				.addOutline(50 * rescaler.getRescaleFactorWindow(), 6 * rescaler.getRescaleFactorWindow(), 1, icon.getOutlineColor().asARGB());
		}

		circleRenderer.draw(tessellator, posMatrix);

		drawContext.disableScissor();
	}

	private void renderIcon(ElementalBurst burstData, ElementalBurstIcon icon, CircleRenderer circleRenderer, DrawContext drawContext, Matrix4f posMatrix, Rescaler rescaler, double percentFilled) {
		final int scaleEB = (int) (112.0 * rescaler.getRescaleFactor());
		final boolean disable = percentFilled > 0 || burstData.isDisabled(client.player) || icon.renderAsDisabled(client.player);

		if (disable) {
			final Color colorDisabled = Color.fromARGBHex(0x80969696);
			
			renderExtraFill(icon, percentFilled, circleRenderer, drawContext, posMatrix);

			RenderSystem.setShaderColor(colorDisabled.getRedAsPercent(), colorDisabled.getGreenAsPercent(), colorDisabled.getBlueAsPercent(), colorDisabled.getAlpha());
		}

		drawContext.drawTexture(icon.getIcon(), rescaler.rescaleX(1763), rescaler.rescaleY(916), 0, 0, scaleEB, scaleEB, scaleEB, scaleEB);

		RenderSystem.setShaderColor(1, 1, 1, 0);

		circleRenderer
			.add(56 * rescaler.getRescaleFactorWindow(), percentFilled, 0x26c8c8c8)
			.addOutline(50 * rescaler.getRescaleFactorWindow(), 6 * rescaler.getRescaleFactorWindow(), percentFilled, 0x99c8c8c8)
			.draw(tessellator, posMatrix);
	}

	private void renderCooldown(ElementalBurst burstData, ElementalBurstIcon icon, DrawContext drawContext, Matrix4f posMatrix, Rescaler rescaler, double percentFilled) {
		// System.out.println(percentFilled);

		if (percentFilled == 0 || !burstData.shouldShowCooldown()) return;
			
		MatrixStack matrices = drawContext.getMatrices();

		matrices.push();
		matrices.scale(1.25F, 1.25F, 1F);
		
		PowerRenderer.drawCenteredText(
			drawContext,
			client.textRenderer,
			PowerRenderer.changeTextFont(Text.literal(String.format("%.1f", ((double) resolveCooldown(icon) / 20))), OriginsGenshin.identifier("genshin")),
			rescaler.rescaleX(1455),
			rescaler.rescaleY(780),
			0xFFFFFFFF,
			false
		);

		matrices.pop();
	}

	private void renderExtraFill(ElementalBurstIcon icon, double percentFilled, CircleRenderer circleRenderer, DrawContext drawContext, Matrix4f posMatrix) {
		if (icon.getColor() == null) return;

		final double resourceMultiplier = this.resolveFillResource(icon);

		if (resourceMultiplier != 1) return;

		circleRenderer
			.add(56 * rescaler.getRescaleFactorWindow(), 1, icon.getColor().from().multiply(1, 1, 1, 0.5).asARGB());

		if (icon.getOutlineColor() != null) {
			circleRenderer
			.addOutline(50 * rescaler.getRescaleFactorWindow(), 6 * rescaler.getRescaleFactorWindow(), 1, icon.getOutlineColor().from().multiply(1.25, 1.25, 1.25, 0.5).asARGB())
			.addOutline(50 * rescaler.getRescaleFactorWindow(), 6 * rescaler.getRescaleFactorWindow(), percentFilled, icon.getOutlineColor().from().multiply(1.5, 1.5, 1.5, 0.5).asARGB());
		}

		circleRenderer.draw(tessellator, posMatrix);
	}

	public double resolveFillResource(ElementalBurstIcon skillIcon) {
		// System.out.println(skillIcon.getResource());
		if (skillIcon.getResource() == null) return -1;

		return PowerRenderer.resolveResourceAsOptional(skillIcon.getResource(), client.player)
			.map(pair -> Math.min(1, Math.max((double) pair.getLeft() / (skillIcon.getNewMax() != -1 ? skillIcon.getNewMax() : pair.getRight()), 0)))
			.orElse((double) -1);
	}
}