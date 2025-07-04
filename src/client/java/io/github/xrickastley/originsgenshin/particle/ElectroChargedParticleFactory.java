package io.github.xrickastley.originsgenshin.particle;

import io.github.xrickastley.originsgenshin.util.Color;
import io.github.xrickastley.originsgenshin.util.TextHelper;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleFactory;
import net.minecraft.client.particle.SpriteProvider;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particle.DefaultParticleType;

public class ElectroChargedParticleFactory implements ParticleFactory<DefaultParticleType> {
	public ElectroChargedParticleFactory(SpriteProvider sp) {}

	public Particle createParticle(DefaultParticleType defaultParticleType, ClientWorld clientWorld, double d, double e, double f, double g, double h, double i) {
		System.out.printf("d: %.2f, e: %.2f, f: %.2f, g: %.2f, h: %.2f, i: %.2f\n", d, e, f, g, h, i);
		return new ReactionParticle(clientWorld, d, e, f, Color.fromRGBAHex("#d691fc").asARGB())
			.setText(TextHelper.withFont("Electro-Charged", TextBillboardParticle.GENSHIN_FONT));
	}
}
