package io.github.xrickastley.originsgenshin.particle;

import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleFactory;
import net.minecraft.client.particle.SpriteProvider;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particle.DefaultParticleType;
import net.minecraft.text.Text;

public class TestParticleFactory implements ParticleFactory<DefaultParticleType> {
	public TestParticleFactory(SpriteProvider sp) {}

	public Particle createParticle(DefaultParticleType defaultParticleType, ClientWorld clientWorld, double d, double e, double f, double g, double h, double i) {
		return new ReactionParticle(clientWorld, d, e, f, 0xffffff)
			.setText(Text.literal("Test!"));
	}
}
