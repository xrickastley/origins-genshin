package io.github.xrickastley.originsgenshin.particle;

import org.lwjgl.opengl.GL11;

import com.mojang.blaze3d.systems.RenderSystem;

import io.github.xrickastley.originsgenshin.OriginsGenshin;
import io.github.xrickastley.originsgenshin.util.Color;
import io.github.xrickastley.originsgenshin.util.Colors;
import io.github.xrickastley.originsgenshin.util.TextHelper;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.font.TextRenderer.TextLayerType;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleFactory;
import net.minecraft.client.particle.SpriteProvider;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particle.DefaultParticleType;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

@Environment(EnvType.CLIENT)
public class DamageTextParticle extends TextBillboardParticle {
	protected DamageTextParticle(ClientWorld clientWorld, double x, double y, double z, double amount, double color) {
		super(clientWorld, x, y, z, color);

		this.collidesWithWorld = false;
		this.gravityStrength = 0f;
		this.velocityY = 0d;
		this.velocityX = -0.001d;
		this.maxAge = 75;
		this.fadeAge = maxAge - 25;
		this.color = MathHelper.floor(color);
		this.setText(TextHelper.changeTextFont(String.format("%d", (int) Math.floor(amount)), TextBillboardParticle.GENSHIN_FONT));
		
		// System.out.println(this.text.toString());
	}

	@Override
	public void buildGeometry(VertexConsumer vertexConsumer, Camera camera, float tickDelta) {
		MinecraftClient client = MinecraftClient.getInstance();
		TextRenderer renderer = client.textRenderer;
		
		Vec3d vec3d = camera.getPos();
		float x = (float)(MathHelper.lerp((double)tickDelta, this.prevPosX, this.x) - vec3d.getX());
		float y = (float)(MathHelper.lerp((double)tickDelta, this.prevPosY, this.y) - vec3d.getY());
		float z = (float)(MathHelper.lerp((double)tickDelta, this.prevPosZ, this.z) - vec3d.getZ());

		if (alpha <= 0f || scale <= 0f) return;

		if (age > scaleAge) y += MathHelper.lerp((double) (age - scaleAge) / (maxAge - scaleAge), 0, 0.5);

		double intAlpha = Math.max(0.0f, MathHelper.lerp(Math.max(0, (double) (age - fadeAge) / (maxAge - fadeAge)), 1.0, 0.0));

		Color fColor = Color
			.fromARGBHex(color)
			.multiply(1, 1, 1, intAlpha);

		int intColor = fColor.asARGB();

		MatrixStack matrixStack = new MatrixStack();
		matrixStack.push();
		matrixStack.translate(x, y, z);
		matrixStack.multiply(camera.getRotation());
		matrixStack.scale(-0.04f * 0.75f, -0.04f * 0.75f, -1f);

		RenderSystem.disableCull();
		RenderSystem.disableDepthTest();
    	RenderSystem.depthFunc(GL11.GL_ALWAYS);

		VertexConsumerProvider.Immediate immediate = VertexConsumerProvider.immediate(Tessellator.getInstance().getBuffer());

		renderer.draw(text, x, 0, intColor, false, matrixStack.peek().getPositionMatrix(), immediate, TextLayerType.POLYGON_OFFSET, Colors.BLANK.asARGB(), LightmapTextureManager.MAX_LIGHT_COORDINATE);
		immediate.draw();

		RenderSystem.enableCull();
		RenderSystem.enableDepthTest();
		RenderSystem.depthFunc(GL11.GL_LEQUAL);
		
		matrixStack.pop();
	}
	
	public static class Factory implements ParticleFactory<DefaultParticleType> {
		public Factory(SpriteProvider sp) {}

		public Particle createParticle(DefaultParticleType defaultParticleType, ClientWorld clientWorld, double x, double y, double z, double velX, double velY, double velZ) {
			OriginsGenshin.sublogger(DamageTextParticle.class).info("Creating particle: DamageTextParticle from DamageTextParticle.Factory");

			// VelX and VelY serve as damage dealt and color, respectively.
			return new DamageTextParticle(clientWorld, x, y, z, velX, velY);
		}
	}
}