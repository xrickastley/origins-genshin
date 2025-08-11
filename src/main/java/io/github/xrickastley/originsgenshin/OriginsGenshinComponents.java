package io.github.xrickastley.originsgenshin;

import io.github.xrickastley.originsgenshin.component.ElementComponent;
import io.github.xrickastley.originsgenshin.component.ElementComponentImpl;
import io.github.xrickastley.originsgenshin.component.FrozenEffectComponent;
import io.github.xrickastley.originsgenshin.component.FrozenEffectComponentImpl;

import net.minecraft.entity.LivingEntity;
import dev.onyxstudios.cca.api.v3.component.ComponentFactory;
import dev.onyxstudios.cca.api.v3.entity.EntityComponentFactoryRegistry;
import dev.onyxstudios.cca.api.v3.entity.EntityComponentInitializer;

import javax.annotation.Nonnull;

public class OriginsGenshinComponents implements EntityComponentInitializer {
	@Override
	public void registerEntityComponentFactories(@Nonnull EntityComponentFactoryRegistry registry) {
		registry.registerFor(LivingEntity.class, ElementComponent.KEY, (ComponentFactory<LivingEntity, ElementComponent>) ElementComponentImpl::new);
		registry.registerFor(LivingEntity.class, FrozenEffectComponent.KEY, (ComponentFactory<LivingEntity, FrozenEffectComponent>) FrozenEffectComponentImpl::new);
	}
}
