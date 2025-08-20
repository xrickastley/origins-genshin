package io.github.xrickastley.originsgenshin.particle;

import io.github.xrickastley.originsgenshin.util.TextHelper;

import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleFactory;
import net.minecraft.client.particle.SpriteProvider;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particle.DefaultParticleType;

public class ShatterParticleFactory implements ParticleFactory<DefaultParticleType> {
	public ShatterParticleFactory(SpriteProvider sp) {}

	public Particle createParticle(DefaultParticleType defaultParticleType, ClientWorld clientWorld, double d, double e, double f, double g, double h, double i) {
		return new ReactionParticle(clientWorld, d, e, f, 0xFFFFFFFF)
			.setText(
				TextHelper.font(
					TextHelper.gradient("Shatter", 0xcfffff, 0x70dee4), 
					TextBillboardParticle.GENSHIN_FONT
				)
			);
	}
}