package io.github.xrickastley.originsgenshin.component;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import net.minecraft.entity.EntityPose;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.MathHelper;

import javax.annotation.Nonnull;

public class FrozenEffectComponentImpl implements FrozenEffectComponent {
	private final LivingEntity owner;
	private boolean isFrozen = false;
	private boolean hadNoAi = false;
	private EntityPose forcePose = EntityPose.STANDING;
	private float forceHeadYaw = 0.0f;
	private float forceBodyYaw = 0.0f;
	private float forcePitch = 0.0f;
	private float forceLimbAngle = 0.0f;
	private float forceLimbDistance = 0.0f;

	public FrozenEffectComponentImpl(LivingEntity owner) {
		this.owner = owner;
	}

	@Override
	public void readFromNbt(@Nonnull NbtCompound tag) {
		this.isFrozen = tag.getBoolean("IsFrozen");
		this.hadNoAi = tag.getBoolean("HadNoAi");
		this.forcePose = EntityPose.valueOf(tag.getString("ForcePose"));
		this.forceHeadYaw = tag.getFloat("ForceHeadYaw");
		this.forceBodyYaw = tag.getFloat("ForceBodyYaw");
		this.forcePitch = tag.getFloat("ForcePitch");
		this.forceLimbAngle = tag.getFloat("ForceLimbAngle");
		this.forceLimbDistance = tag.getFloat("ForceLimbDistance");
	}

	@Override
	public void writeToNbt(@Nonnull NbtCompound tag) {
		tag.putBoolean("IsFrozen", this.isFrozen);
		tag.putBoolean("HadNoAi", this.hadNoAi);
		tag.putString("ForcePose", this.forcePose.toString());
		tag.putFloat("ForceHeadYaw", this.forceHeadYaw);
		tag.putFloat("ForceBodyYaw", this.forceBodyYaw);
		tag.putFloat("ForcePitch", this.forcePitch);
		tag.putFloat("ForceLimbAngle", this.forceLimbAngle);
		tag.putFloat("ForceLimbDistance", this.forceLimbDistance);
	}

	@Override
	public void clientTick() {
		if (!this.isFrozen()) return;

		owner.setPose(this.forcePose);
		owner.setHeadYaw(this.forceBodyYaw);
		owner.setBodyYaw(this.forceBodyYaw);
		owner.setPitch(this.forcePitch);
	}

	public boolean isFrozen() {
		return this.isFrozen;
	}

	public EntityPose getForcePose() {
		return this.forcePose;
	}

	public float getForceHeadYaw() {
		return this.forceHeadYaw;
	}

	public float getForceBodyYaw() {
		return this.forceBodyYaw;
	}

	public float getForcePitch() {
		return this.forcePitch;
	}

	public float getForceLimbAngle() {
		return this.forceLimbAngle;
	}

	public float getForceLimbDistance() {
		return this.forceLimbDistance;
	}

	public void freeze() {
		this.isFrozen = true;
		this.hadNoAi = this.getSubclassValue(MobEntity.class, MobEntity::isAiDisabled, () -> false);
		this.forcePose = owner.getPose();
		this.forceHeadYaw = owner.getHeadYaw();
		this.forceBodyYaw = owner.getBodyYaw();
		this.forcePitch = owner.getPitch();
		this.forceLimbAngle = MathHelper.nextFloat(owner.getRandom(), 0, 0.5f);
		this.forceLimbDistance = MathHelper.nextFloat(owner.getRandom(), -0.5f, 0.5f);

		owner.setSilent(true);
		this.onlyIfSubclass(MobEntity.class, mob -> mob.setAiDisabled(true));

		FrozenEffectComponent.sync(owner);
	}

	public void unfreeze() {
		this.isFrozen = false;

		owner.setSilent(false);
		this.onlyIfSubclass(MobEntity.class, mob -> mob.setAiDisabled(this.hadNoAi));

		FrozenEffectComponent.sync(owner);
	}

	@SuppressWarnings("unchecked")
	private <T, C extends LivingEntity> T getSubclassValue(Class<C> clazz, Function<C, T> ifClazz, Supplier<T> ifNot) {
		return clazz.isInstance(owner)
			? ifClazz.apply((C) owner)
			: ifNot.get();
	}

	@SuppressWarnings("unchecked")
	private <C extends LivingEntity> void onlyIfSubclass(Class<C> clazz, Consumer<C> ifClazz) {
		if (clazz.isInstance(owner)) ifClazz.accept((C) owner);
	}
}
