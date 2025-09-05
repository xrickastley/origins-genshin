package io.github.xrickastley.originsgenshin.registry;

import io.github.xrickastley.originsgenshin.OriginsGenshin;

import net.minecraft.entity.damage.DamageType;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;

public class OriginsGenshinDamageTypeTags {
	public static final TagKey<DamageType> PREVENTS_COOLDOWN_TRIGGER = OriginsGenshinDamageTypeTags.of("prevents_cooldown_trigger");
	public static final TagKey<DamageType> HAS_PYRO_INFUSION = OriginsGenshinDamageTypeTags.of("has_pyro_infusion");
	public static final TagKey<DamageType> HAS_HYDRO_INFUSION = OriginsGenshinDamageTypeTags.of("has_hydro_infusion");
	public static final TagKey<DamageType> HAS_ELECTRO_INFUSION = OriginsGenshinDamageTypeTags.of("has_electro_infusion");
	public static final TagKey<DamageType> HAS_ANEMO_INFUSION = OriginsGenshinDamageTypeTags.of("has_anemo_infusion");
	public static final TagKey<DamageType> HAS_DENDRO_INFUSION = OriginsGenshinDamageTypeTags.of("has_dendro_infusion");
	public static final TagKey<DamageType> HAS_CRYO_INFUSION = OriginsGenshinDamageTypeTags.of("has_cryo_infusion");
	public static final TagKey<DamageType> HAS_GEO_INFUSION = OriginsGenshinDamageTypeTags.of("has_geo_infusion");

	private static TagKey<DamageType> of(String path) {
		return TagKey.of(RegistryKeys.DAMAGE_TYPE, OriginsGenshin.identifier(path));
	}
}
