package io.github.xrickastley.originsgenshin.factory;

import io.github.xrickastley.originsgenshin.OriginsGenshin;
import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes;
import net.minecraft.particle.DefaultParticleType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

public class OriginsGenshinParticleFactory {
	public static final DefaultParticleType PYRO = register("pyro");
	public static final DefaultParticleType HYDRO = register("hydro");
	public static final DefaultParticleType ANEMO = register("anemo");
	public static final DefaultParticleType ELECTRO = register("electro");
	public static final DefaultParticleType DENDRO = register("dendro");
	public static final DefaultParticleType CRYO = register("cryo");
	public static final DefaultParticleType GEO = register("geo");

	public static final DefaultParticleType OVERLOADED = register("overloaded");
	public static final DefaultParticleType ELECTRO_CHARGED = register("electro-charged");
	public static final DefaultParticleType SUPERCONDUCT = register("superconduct");
	public static final DefaultParticleType FROZEN = register("frozen");
	public static final DefaultParticleType QUICKEN = register("quicken");
	public static final DefaultParticleType BLOOM = register("bloom");
	public static final DefaultParticleType MELT = register("melt");
	public static final DefaultParticleType VAPORIZE = register("vaporize");
	public static final DefaultParticleType SWIRL = register("swirl");
	public static final DefaultParticleType BURNING = register("burning");
	public static final DefaultParticleType SPREAD = register("spread");
	public static final DefaultParticleType AGGRAVATE = register("aggravate");
	public static final DefaultParticleType CRYSTALLIZE = register("crystallize");

	public static final DefaultParticleType DAMAGE_TEXT = register("damage_text");
	public static final DefaultParticleType TEST = register("test");

	private static DefaultParticleType register(String name) {
        return Registry.register(Registries.PARTICLE_TYPE, OriginsGenshin.identifier(name), FabricParticleTypes.simple());
	}
}
