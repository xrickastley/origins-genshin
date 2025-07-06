package io.github.xrickastley.originsgenshin.component;

import dev.onyxstudios.cca.api.v3.component.ComponentKey;
import dev.onyxstudios.cca.api.v3.component.ComponentRegistry;
import dev.onyxstudios.cca.api.v3.component.sync.AutoSyncedComponent;
import dev.onyxstudios.cca.api.v3.component.tick.ClientTickingComponent;
import io.github.xrickastley.originsgenshin.OriginsGenshin;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityPose;

public interface FrozenEffectComponent extends AutoSyncedComponent, ClientTickingComponent {
	public static final ComponentKey<FrozenEffectComponent> KEY = ComponentRegistry.getOrCreate(OriginsGenshin.identifier("frozen_effect"), FrozenEffectComponent.class);

	public boolean isFrozen();

	public EntityPose getForcePose();

	public float getForceHeadYaw();

	public float getForceBodyYaw();

	public float getForcePitch();

	public float getForceLimbAngle();

	public float getForceLimbDistance();

	public void freeze();
	
	public void unfreeze();

	public static void sync(Entity entity) {
		KEY.sync(entity);
	}
}
