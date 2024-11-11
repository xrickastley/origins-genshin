package io.github.xrickastley.originsgenshin.factory;

import java.util.function.Supplier;

import io.github.xrickastley.originsgenshin.OriginsGenshin;
import io.github.xrickastley.originsgenshin.entity.DendroCoreEntity;

import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

public class OriginsGenshinEntities {
	public static final EntityType<DendroCoreEntity> DENDRO_CORE = FabricEntityTypeBuilder
		.<DendroCoreEntity>create(SpawnGroup.MISC, DendroCoreEntity::new)
		.dimensions(EntityDimensions.fixed(0.5f, 0.6875f))
		.trackRangeBlocks(24)
		.build();
	
	public static void register() {
		register("dendro_core", OriginsGenshinEntities.DENDRO_CORE, DendroCoreEntity::getAttributeBuilder);
	}

	private static <T extends LivingEntity> EntityType<T> register(String id, EntityType<T> entityType, Supplier<DefaultAttributeContainer.Builder> builderSupplier) {
		return register(id, entityType, builderSupplier.get());
	}

	private static <T extends LivingEntity> EntityType<T> register(String id, EntityType<T> entityType, DefaultAttributeContainer.Builder builder) {
		FabricDefaultAttributeRegistry.register(entityType, builder);
		
		return Registry.register(Registries.ENTITY_TYPE, OriginsGenshin.identifier(id), entityType);
	}
}
