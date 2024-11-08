package io.github.xrickastley.originsgenshin.particle;

import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleFactory;
import net.minecraft.client.particle.ParticleTextureSheet;
import net.minecraft.client.particle.SpriteBillboardParticle;
import net.minecraft.client.particle.SpriteProvider;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particle.DefaultParticleType;

public class ElementParticle extends SpriteBillboardParticle {
	private final SpriteProvider spriteProvider;

	public ElementParticle(ClientWorld world, double x, double y, double z, double velX, double velY, double velZ, SpriteProvider spriteProvider) {
		super(world, x, y, z);
		this.spriteProvider = spriteProvider;

		this.maxAge = 1;
		this.scale = 0.30f;
		this.angle = 0;
		this.alpha = 1.0f;
		this.collidesWithWorld = false;

		this.velocityX = 0;
		this.velocityY = 0;
		this.velocityZ = 0;
		this.x = x;
		this.y = y;
		this.z = z;
		this.setSpriteForAge(spriteProvider);
	}

	@Override
	public void tick() {
		// System.out.println("EE");

		this.prevPosX = this.x;
		this.prevPosY = this.y;
		this.prevPosZ = this.z;
		this.prevAngle = this.angle;

		if (this.age++ >= this.maxAge || this.scale <= 0 || this.alpha <= 0) {
			this.markDead();
		} else {
			this.setSpriteForAge(this.spriteProvider);
			this.move(this.velocityX, this.velocityY, this.velocityZ);
		}
	}

	@Override
	public void buildGeometry(VertexConsumer vertexConsumer, Camera camera, float tickDelta) {
		super.buildGeometry(vertexConsumer, camera, tickDelta);
	}

	@Override
	public ParticleTextureSheet getType() {
		return ParticleTextureSheet.PARTICLE_SHEET_TRANSLUCENT; //Allows for the texture to have some transparency
	}

	public static class Factory implements ParticleFactory<DefaultParticleType> {
		private final SpriteProvider spriteProvider;

		public Factory(SpriteProvider spriteProvider) {
			this.spriteProvider = spriteProvider;
		}

		public Particle createParticle(DefaultParticleType defaultParticleType, ClientWorld world, double x, double y, double z, double velX, double velY, double velZ) {
			return new ElementParticle(world, x, y, z, velX, velY, velZ, this.spriteProvider);
		}
	}
}
