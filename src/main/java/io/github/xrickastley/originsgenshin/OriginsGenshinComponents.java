package io.github.xrickastley.originsgenshin;

import io.github.xrickastley.originsgenshin.component.ElementComponent;
import io.github.xrickastley.originsgenshin.component.ElementComponentImpl;
import io.github.xrickastley.originsgenshin.component.ElementalInfusionComponent;
import io.github.xrickastley.originsgenshin.component.FrozenEffectComponent;
import io.github.xrickastley.originsgenshin.component.FrozenEffectComponentImpl;

import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ToolItem;

import dev.onyxstudios.cca.api.v3.component.ComponentFactory;
import dev.onyxstudios.cca.api.v3.entity.EntityComponentFactoryRegistry;
import dev.onyxstudios.cca.api.v3.entity.EntityComponentInitializer;
import dev.onyxstudios.cca.api.v3.item.ItemComponentFactoryRegistry;
import dev.onyxstudios.cca.api.v3.item.ItemComponentInitializer;

import javax.annotation.Nonnull;

public class OriginsGenshinComponents implements EntityComponentInitializer, ItemComponentInitializer {
	@Override
	public void registerEntityComponentFactories(@Nonnull EntityComponentFactoryRegistry registry) {
		registry.registerFor(LivingEntity.class, ElementComponent.KEY, (ComponentFactory<LivingEntity, ElementComponent>) ElementComponentImpl::new);
		registry.registerFor(LivingEntity.class, FrozenEffectComponent.KEY, (ComponentFactory<LivingEntity, FrozenEffectComponent>) FrozenEffectComponentImpl::new);
	}

	@Override
	public void registerItemComponentFactories(@Nonnull ItemComponentFactoryRegistry registry) {
		registry.register(item -> item instanceof ToolItem, ElementalInfusionComponent.KEY, ElementalInfusionComponent::new);
	}
}
