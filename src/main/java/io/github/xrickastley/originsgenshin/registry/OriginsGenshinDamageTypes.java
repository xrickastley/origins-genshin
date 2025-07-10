package io.github.xrickastley.originsgenshin.registry;

import io.github.xrickastley.originsgenshin.OriginsGenshin;
import net.minecraft.entity.damage.DamageType;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;

public class OriginsGenshinDamageTypes {
	public static final RegistryKey<DamageType> BURNING = RegistryKey.of(RegistryKeys.DAMAGE_TYPE, OriginsGenshin.identifier("burning"));
	public static final RegistryKey<DamageType> DENDRO_CORE = RegistryKey.of(RegistryKeys.DAMAGE_TYPE, OriginsGenshin.identifier("dendro_core"));
	public static final RegistryKey<DamageType> ELECTRO_CHARGED = RegistryKey.of(RegistryKeys.DAMAGE_TYPE, OriginsGenshin.identifier("electro-charged"));
}
