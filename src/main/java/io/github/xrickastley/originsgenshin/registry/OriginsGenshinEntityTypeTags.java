package io.github.xrickastley.originsgenshin.registry;

import io.github.xrickastley.originsgenshin.OriginsGenshin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;

public class OriginsGenshinEntityTypeTags {
	public static final TagKey<EntityType<? extends Entity>> DEALS_PYRO_DAMAGE = OriginsGenshinEntityTypeTags.of("deals_pyro_damage");
	public static final TagKey<EntityType<? extends Entity>> DEALS_HYDRO_DAMAGE = OriginsGenshinEntityTypeTags.of("deals_hydro_damage");
	public static final TagKey<EntityType<? extends Entity>> DEALS_ELECTRO_DAMAGE = OriginsGenshinEntityTypeTags.of("deals_electro_damage");
	public static final TagKey<EntityType<? extends Entity>> DEALS_ANEMO_DAMAGE = OriginsGenshinEntityTypeTags.of("deals_anemo_damage");
	public static final TagKey<EntityType<? extends Entity>> DEALS_DENDRO_DAMAGE = OriginsGenshinEntityTypeTags.of("deals_dendro_damage");
	public static final TagKey<EntityType<? extends Entity>> DEALS_CRYO_DAMAGE = OriginsGenshinEntityTypeTags.of("deals_cryo_damage");
	public static final TagKey<EntityType<? extends Entity>> DEALS_GEO_DAMAGE = OriginsGenshinEntityTypeTags.of("deals_geo_damage");

	private static TagKey<EntityType<? extends Entity>> of(String path) {
		return TagKey.of(RegistryKeys.ENTITY_TYPE, OriginsGenshin.identifier(path));
	}
}
