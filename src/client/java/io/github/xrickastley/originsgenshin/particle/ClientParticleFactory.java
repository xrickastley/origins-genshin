package io.github.xrickastley.originsgenshin.particle;

import io.github.xrickastley.originsgenshin.OriginsGenshin;
import io.github.xrickastley.originsgenshin.factory.OriginsGenshinParticleFactory;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry.PendingParticleFactory;

import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleType;

@Environment(EnvType.CLIENT)
public class ClientParticleFactory {
	public static void register() {
		try {
			OriginsGenshin
				.sublogger(ClientParticleFactory.class)
				.info("Registering Particles!");

			register(OriginsGenshinParticleFactory.Pyro, ElementParticle.Factory::new);
			register(OriginsGenshinParticleFactory.Hydro, ElementParticle.Factory::new);
			register(OriginsGenshinParticleFactory.Anemo, ElementParticle.Factory::new);
			register(OriginsGenshinParticleFactory.Electro, ElementParticle.Factory::new);
			register(OriginsGenshinParticleFactory.Dendro, ElementParticle.Factory::new);
			register(OriginsGenshinParticleFactory.Cryo, ElementParticle.Factory::new);
			register(OriginsGenshinParticleFactory.Geo, ElementParticle.Factory::new);
	
			register(OriginsGenshinParticleFactory.Overloaded, OverloadedParticleFactory::new);
			register(OriginsGenshinParticleFactory.ElectroCharged, ElectroChargedParticleFactory::new);
			register(OriginsGenshinParticleFactory.Superconduct, SuperconductParticleFactory::new);
			register(OriginsGenshinParticleFactory.Frozen, FrozenParticleFactory::new);
			register(OriginsGenshinParticleFactory.Bloom, BloomParticleFactory::new);
			register(OriginsGenshinParticleFactory.Vaporize, VaporizeParticleFactory::new);
			register(OriginsGenshinParticleFactory.Melt, MeltParticleFactory::new);
			
			register(OriginsGenshinParticleFactory.DAMAGE_TEXT, DamageTextParticle.Factory::new);
		} catch (Exception e) {
			OriginsGenshin
				.sublogger(ClientParticleFactory.class)
				.error("An exception occured while trying to register Particles", e);
		}
	}

	private static <P extends ParticleEffect> void register(ParticleType<P> particle, PendingParticleFactory<P> factory) {
        ParticleFactoryRegistry.getInstance().register(particle, factory);
	}
}

