package io.github.xrickastley.originsgenshin.particle;

import io.github.xrickastley.originsgenshin.factory.OriginsGenshinParticleFactory;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry.PendingParticleFactory;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleType;

@Environment(EnvType.CLIENT)
public class ClientParticleFactory {
	public static void register() {
		register(OriginsGenshinParticleFactory.PYRO, ElementParticle.Factory::new);
		register(OriginsGenshinParticleFactory.HYDRO, ElementParticle.Factory::new);
		register(OriginsGenshinParticleFactory.ANEMO, ElementParticle.Factory::new);
		register(OriginsGenshinParticleFactory.ELECTRO, ElementParticle.Factory::new);
		register(OriginsGenshinParticleFactory.DENDRO, ElementParticle.Factory::new);
		register(OriginsGenshinParticleFactory.CRYO, ElementParticle.Factory::new);
		register(OriginsGenshinParticleFactory.GEO, ElementParticle.Factory::new);

		register(OriginsGenshinParticleFactory.OVERLOADED, OverloadedParticleFactory::new);
		register(OriginsGenshinParticleFactory.ELECTRO_CHARGED, ElectroChargedParticleFactory::new);
		register(OriginsGenshinParticleFactory.SUPERCONDUCT, SuperconductParticleFactory::new);
		register(OriginsGenshinParticleFactory.FROZEN, FrozenParticleFactory::new);
		register(OriginsGenshinParticleFactory.QUICKEN, QuickenParticleFactory::new);
		register(OriginsGenshinParticleFactory.BLOOM, BloomParticleFactory::new);
		register(OriginsGenshinParticleFactory.MELT, MeltParticleFactory::new);
		register(OriginsGenshinParticleFactory.VAPORIZE, VaporizeParticleFactory::new);
		register(OriginsGenshinParticleFactory.SWIRL, SwirlParticleFactory::new);
		register(OriginsGenshinParticleFactory.BURNING, BurningParticleFactory::new);
		register(OriginsGenshinParticleFactory.SPREAD, SpreadParticleFactory::new);
		register(OriginsGenshinParticleFactory.AGGRAVATE, AggravateParticleFactory::new);
		register(OriginsGenshinParticleFactory.CRYSTALLIZE, CrystallizeParticleFactory::new);
		register(OriginsGenshinParticleFactory.HYPERBLOOM, HyperbloomParticleFactory::new);
		register(OriginsGenshinParticleFactory.BURGEON, BurgeonParticleFactory::new);
		register(OriginsGenshinParticleFactory.SHATTER, ShatterParticleFactory::new);

		register(OriginsGenshinParticleFactory.DAMAGE_TEXT, DamageTextParticle.Factory::new);
	}

	private static <P extends ParticleEffect> void register(ParticleType<P> particle, PendingParticleFactory<P> factory) {
		ParticleFactoryRegistry.getInstance().register(particle, factory);
	}
}

