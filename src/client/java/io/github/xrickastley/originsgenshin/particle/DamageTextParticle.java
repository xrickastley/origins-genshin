package io.github.xrickastley.originsgenshin.particle;

import com.mojang.blaze3d.systems.RenderSystem;

import io.github.xrickastley.originsgenshin.OriginsGenshin;
import io.github.xrickastley.originsgenshin.util.Color;
import io.github.xrickastley.originsgenshin.util.DelayedRenderer;
import io.github.xrickastley.originsgenshin.util.Ease;
import io.github.xrickastley.originsgenshin.util.TextHelper;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleFactory;
import net.minecraft.client.particle.SpriteProvider;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particle.DefaultParticleType;
import net.minecraft.util.math.MathHelper;

@Environment(EnvType.CLIENT)
public class DamageTextParticle extends TextBillboardParticle {
	protected DamageTextParticle(ClientWorld clientWorld, double x, double y, double z, double amount, double color) {
		super(clientWorld, x, y, z, color);

		this.collidesWithWorld = false;
		this.gravityStrength = 0f;
		this.velocityY = 0d;
		this.velocityX = -0.001d;
		this.maxAge = 40;
		this.fadeAge = maxAge - 15;
		this.color = MathHelper.floor(color);
		this.setText(TextHelper.withFont(String.format("%d", (int) Math.max(amount, 1)), TextBillboardParticle.GENSHIN_FONT));
	}

	@Override
	public void buildGeometry(VertexConsumer vertexConsumer, Camera camera, float f) {
		DelayedRenderer.add((tickDelta, matrices) -> render(camera, tickDelta, matrices));
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

	public static class Factory implements ParticleFactory<DefaultParticleType> {
		public Factory(SpriteProvider sp) {}

		public Particle createParticle(DefaultParticleType defaultParticleType, ClientWorld clientWorld, double x, double y, double z, double velX, double velY, double velZ) {
			// OriginsGenshin.sublogger(DamageTextParticle.class).info("Creating particle: DamageTextParticle from DamageTextParticle.Factory");

			// VelX and VelY serve as damage dealt and color, respectively.
			return new DamageTextParticle(clientWorld, x, y, z, velX, velY);
		}
	}
}