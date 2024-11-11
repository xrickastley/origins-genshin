package io.github.xrickastley.originsgenshin.factory;

import io.github.xrickastley.originsgenshin.OriginsGenshin;
import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes;
import net.minecraft.particle.DefaultParticleType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

public class OriginsGenshinParticleFactory {
	public static final DefaultParticleType Pyro = register("pyro");
	public static final DefaultParticleType Hydro = register("hydro");
	public static final DefaultParticleType Anemo = register("anemo");
	public static final DefaultParticleType Electro = register("electro");
	public static final DefaultParticleType Dendro = register("dendro");
	public static final DefaultParticleType Cryo = register("cryo");
	public static final DefaultParticleType Geo = register("geo");

	public static final DefaultParticleType Overloaded = register("overloaded");
	public static final DefaultParticleType ElectroCharged = register("electro-charged");
	public static final DefaultParticleType Superconduct = register("superconduct");
	public static final DefaultParticleType Frozen = register("frozen");
	public static final DefaultParticleType Quicken = register("quicken");
	public static final DefaultParticleType Bloom = register("bloom");
	public static final DefaultParticleType Melt = register("melt");
	public static final DefaultParticleType Vaporize = register("vaporize");
	public static final DefaultParticleType Swirl = register("swirl");

	public static final DefaultParticleType DAMAGE_TEXT = register("damage_text");

	private static DefaultParticleType register(String name) {
        return Registry.register(Registries.PARTICLE_TYPE, OriginsGenshin.identifier(name), FabricParticleTypes.simple());
	}
}
