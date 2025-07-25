package io.github.xrickastley.originsgenshin.particle;

import io.github.xrickastley.originsgenshin.util.Colors;
import io.github.xrickastley.originsgenshin.util.TextHelper;

import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleFactory;
import net.minecraft.client.particle.SpriteProvider;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particle.DefaultParticleType;

public class BurgeonParticleFactory implements ParticleFactory<DefaultParticleType> {
	public BurgeonParticleFactory(SpriteProvider sp) {}

	public Particle createParticle(DefaultParticleType defaultParticleType, ClientWorld clientWorld, double d, double e, double f, double g, double h, double i) {
		// TODO: check for accuracy in-game
		return new ReactionParticle(clientWorld, d, e, f, Colors.PYRO.asARGB())
			.setText(TextHelper.withFont("Burgeon", TextBillboardParticle.GENSHIN_FONT));
	}
}
