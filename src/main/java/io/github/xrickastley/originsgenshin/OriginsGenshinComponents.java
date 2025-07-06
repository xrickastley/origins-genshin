package io.github.xrickastley.originsgenshin;

import javax.annotation.Nonnull;

import dev.onyxstudios.cca.api.v3.entity.EntityComponentFactoryRegistry;
import dev.onyxstudios.cca.api.v3.entity.EntityComponentInitializer;
import io.github.xrickastley.originsgenshin.component.ElementComponent;
import io.github.xrickastley.originsgenshin.component.ElementComponentImpl;
import io.github.xrickastley.originsgenshin.component.FrozenEffectComponent;
import io.github.xrickastley.originsgenshin.component.FrozenEffectComponentImpl;
import net.minecraft.entity.LivingEntity;

public class OriginsGenshinComponents implements EntityComponentInitializer {
	@Override
	public void registerEntityComponentFactories(@Nonnull EntityComponentFactoryRegistry registry) {
		registry.registerFor(LivingEntity.class, ElementComponent.KEY, ElementComponentImpl::new);
		registry.registerFor(LivingEntity.class, FrozenEffectComponent.KEY, FrozenEffectComponentImpl::new);
	}
}
