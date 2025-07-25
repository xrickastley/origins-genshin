package io.github.xrickastley.originsgenshin.particle;

import io.github.xrickastley.originsgenshin.util.Color;
import io.github.xrickastley.originsgenshin.util.TextHelper;

import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleFactory;
import net.minecraft.client.particle.SpriteProvider;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particle.DefaultParticleType;

public class FrozenParticleFactory implements ParticleFactory<DefaultParticleType> {
	public FrozenParticleFactory(SpriteProvider sp) {}

	public Particle createParticle(DefaultParticleType defaultParticleType, ClientWorld clientWorld, double d, double e, double f, double g, double h, double i) {
		return new ReactionParticle(clientWorld, d, e, f, Color.fromRGBAHex("#b4ffff").asARGB())
			.setText(TextHelper.withFont("Frozen", TextBillboardParticle.GENSHIN_FONT));
	}
}
