package io.github.xrickastley.originsgenshin.particle;

import org.lwjgl.opengl.GL11;

import com.mojang.blaze3d.systems.RenderSystem;

import io.github.xrickastley.originsgenshin.util.Color;
import io.github.xrickastley.originsgenshin.util.Colors;
import io.github.xrickastley.originsgenshin.util.Ease;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.font.TextRenderer.TextLayerType;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

@Environment(EnvType.CLIENT)
public class ReactionParticle extends TextBillboardParticle {
	protected ReactionParticle(ClientWorld clientWorld, double x, double y, double z, double color) {
		super(clientWorld, x, y, z, color);

		System.out.println("HELLO");

		this.collidesWithWorld = false;
		this.gravityStrength = 0f;
		this.velocityY = 0d;
		this.maxAge = 75;
		this.fadeAge = maxAge - 25;
		this.scaleAge = 5;
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

	@Override
	public void buildGeometry(VertexConsumer vertexConsumer, Camera camera, float tickDelta) {
		// System.out.println("HELLO");

		MinecraftClient client = MinecraftClient.getInstance();
		TextRenderer renderer = client.textRenderer;
		
		Vec3d vec3d = camera.getPos();
		float x = (float)(MathHelper.lerp((double)tickDelta, this.prevPosX, this.x) - vec3d.getX());
		float y = (float)(MathHelper.lerp((double)tickDelta, this.prevPosY, this.y) - vec3d.getY());
		float z = (float)(MathHelper.lerp((double)tickDelta, this.prevPosZ, this.z) - vec3d.getZ());

		// System.out.println(alpha);
		// System.out.println(scale);
		// System.out.println(text);

		if (alpha <= 0f || scale <= 0f) return;

		if (age > scaleAge) y += (float) Ease.OUT_SINE.lerpedApply(age, scaleAge, maxAge) * 1.5f;

		double intAlpha = Math.max(0.0f, MathHelper.lerp(Math.max(0, (double) (age - fadeAge) / (maxAge - fadeAge)), 1.0, 0.0));

		Color fColor = Color
			.fromARGBHex(color)
			.multiply(1, 1, 1, intAlpha);

		int intColor = fColor.asARGB();

		float scale = (float) (1.25 - (Ease.IN_OUT_SINE.lerpedApply((age + tickDelta) / 2.5, 0, 1) * 0.5));

		
		MatrixStack matrixStack = new MatrixStack();
		matrixStack.push();
		matrixStack.translate(x + scale, y, z - scale);
		matrixStack.multiply(camera.getRotation());
		matrixStack.scale(-0.04f * scale, -0.04f * scale, -1f);

		RenderSystem.disableCull();
		RenderSystem.disableDepthTest();
    	RenderSystem.depthFunc(GL11.GL_ALWAYS);

		VertexConsumerProvider.Immediate immediate = VertexConsumerProvider.immediate(Tessellator.getInstance().getBuffer());

		renderer.draw(text, x, 0, intColor, false, matrixStack.peek().getPositionMatrix(), immediate, TextLayerType.NORMAL, Colors.BLANK.asARGB(), LightmapTextureManager.MAX_LIGHT_COORDINATE);
		immediate.draw();

		RenderSystem.enableCull();
		RenderSystem.enableDepthTest();
		RenderSystem.depthFunc(GL11.GL_LEQUAL);
		
		matrixStack.pop();
	}
}