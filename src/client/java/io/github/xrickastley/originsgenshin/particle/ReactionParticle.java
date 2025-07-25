package io.github.xrickastley.originsgenshin.particle;

import com.mojang.blaze3d.systems.RenderSystem;

import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;

import io.github.xrickastley.originsgenshin.util.Color;
import io.github.xrickastley.originsgenshin.util.Colors;
import io.github.xrickastley.originsgenshin.util.DelayedRenderer;
import io.github.xrickastley.originsgenshin.util.Ease;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer.TextLayerType;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.text.MutableText;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

@Environment(EnvType.CLIENT)
public class ReactionParticle extends TextBillboardParticle {
	protected ReactionParticle(ClientWorld clientWorld, double x, double y, double z, double color) {
		super(clientWorld, x, y, z, color);

		this.collidesWithWorld = false;
		this.gravityStrength = 0f;
		this.velocityY = 0d;
		this.maxAge = 30;
		this.fadeAge = maxAge - 15;
		this.scaleAge = 8;
		this.color = MathHelper.floor(color);
	}

	@Override
	protected ReactionParticle setText(String text) {
		return this.setText(text, Style.EMPTY);
	}

	@Override
	protected ReactionParticle setText(String text, Style style) {
		return this.setText(Text.literal(text).setStyle(style));
	}

	@Override
	protected ReactionParticle setText(MutableText text) {
		this.text = text.asOrderedText();

		return this;
	}

	public void buildGeometry_v1(VertexConsumer vertexConsumer, Camera camera, float tickDelta) {
		MinecraftClient client = MinecraftClient.getInstance();
		TextRenderer renderer = client.textRenderer;

		Vec3d vec3d = camera.getPos();
		float x = (float) (MathHelper.lerp(tickDelta, this.prevPosX, this.x) - vec3d.getX());
		float y = (float) (MathHelper.lerp(tickDelta, this.prevPosY, this.y) - vec3d.getY());
		float z = (float) (MathHelper.lerp(tickDelta, this.prevPosZ, this.z) - vec3d.getZ());

		if (this.alpha <= 0f || this.scale <= 0f) return;

		y += (float) Ease.OUT_SINE.applyLerpProgress(age, 0, maxAge) * 0.75f;

		double intAlpha = Math.max(0.0f, MathHelper.lerp(Math.max(0, (double) (age - fadeAge) / (maxAge - fadeAge)), 1.0, 0.0));

		Color fColor = Color
			.fromARGBHex(color)
			.multiply(1, 1, 1, intAlpha);

		int intColor = fColor.asARGB();

		float scale = (float) (1.00 - Ease.IN_OUT_QUART.applyLerpProgress((age + tickDelta) / 2.5, 0, 0.25));

		MatrixStack matrixStack = new MatrixStack();
		matrixStack.push();
		matrixStack.translate(x, y, z);
		matrixStack.multiply(camera.getRotation());
		matrixStack.scale(-0.04f * scale, -0.04f * scale, -1f);

		RenderSystem.disableCull();
		RenderSystem.depthMask(true);
		RenderSystem.enableDepthTest();
		RenderSystem.enableBlend();
		RenderSystem.blendFunc(GL11.GL_ONE, GL11.GL_ZERO);
		RenderSystem.depthFunc(GL11.GL_ALWAYS);

		VertexConsumerProvider.Immediate immediate = VertexConsumerProvider.immediate(Tessellator.getInstance().getBuffer());

		renderer.draw(text, x, 0, intColor, false, matrixStack.peek().getPositionMatrix(), immediate, TextLayerType.SEE_THROUGH, Colors.BLANK.asARGB(), LightmapTextureManager.MAX_LIGHT_COORDINATE);
		immediate.draw();

		// RenderSystem.depthFunc(GL11.GL_LEQUAL);

		// RenderSystem.enableDepthTest();

		matrixStack.pop();
	}

	public void render(Camera camera, float tickDelta, MatrixStack matrices) {
		final MinecraftClient client = MinecraftClient.getInstance();
		final VertexConsumerProvider.Immediate immediate = client.getBufferBuilders().getEntityVertexConsumers();

		final float deltaTime = age + tickDelta;

		final double alpha = Math.max(0.0f, MathHelper.lerp((deltaTime - fadeAge) / (maxAge - fadeAge), 1.0, 0.0));
		final float scale = (float) (1.25 - (Ease.IN_OUT_QUART.applyLerpProgress(((age + tickDelta) / scaleAge), 0, 1) * 0.5));

		if (alpha <= 0f || scale <= 0f) return;

		final float x = (float) MathHelper.lerp(tickDelta, this.prevPosX, this.x);
		final float y = (float) MathHelper.lerp(tickDelta, this.prevPosY, this.y) + (float) (Ease.OUT_SINE.applyLerpProgress(age, 0, maxAge) * 0.75f);
		final float z = (float) MathHelper.lerp(tickDelta, this.prevPosZ, this.z);

		final int color = Color
			.fromARGBHex(this.color)
			.multiply(1, 1, 1, alpha)
			.asARGB();

		RenderSystem.disableCull();
		RenderSystem.disableDepthTest();

		ReactionParticle.drawString(camera, matrices, immediate, this.text, x, y, z, color, 0.04f * scale, true, 0f, true);

		immediate.draw();

		RenderSystem.enableCull();
		RenderSystem.enableDepthTest();
	}

	public void buildGeometry(VertexConsumer consumer, final Camera camera, float f) {
		DelayedRenderer.add((tickDelta, matrices) -> render(camera, tickDelta, matrices));
	}

	public static void drawString(final Camera camera, final MatrixStack matrices, final VertexConsumerProvider vertexConsumers, final OrderedText text, final double x, final double y, final double z, final int color, final float size, final boolean center, final float offset, final boolean visibleThroughObjects) {
		final MinecraftClient client = MinecraftClient.getInstance();
		final TextRenderer textRenderer = client.textRenderer;

		final double d = camera.getPos().x;
		final double e = camera.getPos().y;
		final double f = camera.getPos().z;

		matrices.push();
		matrices.translate((float) (x - d), (float) (y - e), (float) (z - f));
		matrices.multiplyPositionMatrix(new Matrix4f().rotation(camera.getRotation()));
		matrices.scale(-size, -size, size);

		float g = center ? (-textRenderer.getWidth(text) / 2.0f) : 0.0f;
		g -= offset / size;

		textRenderer.draw(text, g, 0.0f, color, false, matrices.peek().getPositionMatrix(), vertexConsumers, visibleThroughObjects ? TextLayerType.SEE_THROUGH : TextLayerType.NORMAL, 0, 15728880);

		matrices.pop();
	}
}