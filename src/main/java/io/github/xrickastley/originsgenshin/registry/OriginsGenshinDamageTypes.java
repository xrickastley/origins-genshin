package io.github.xrickastley.originsgenshin.registry;

import io.github.xrickastley.originsgenshin.OriginsGenshin;
import net.minecraft.entity.damage.DamageType;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;

public class OriginsGenshinDamageTypes {
	public static final RegistryKey<DamageType> DENDRO_CORE = RegistryKey.of(RegistryKeys.DAMAGE_TYPE, OriginsGenshin.identifier("dendro_core"));
}
