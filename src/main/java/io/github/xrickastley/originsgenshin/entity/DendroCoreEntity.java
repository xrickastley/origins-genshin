package io.github.xrickastley.originsgenshin.entity;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import javax.annotation.Nullable;

import io.github.xrickastley.originsgenshin.OriginsGenshin;
import io.github.xrickastley.originsgenshin.component.ElementComponent;
import io.github.xrickastley.originsgenshin.element.Element;
import io.github.xrickastley.originsgenshin.element.ElementalApplications;
import io.github.xrickastley.originsgenshin.element.ElementalDamageSource;
import io.github.xrickastley.originsgenshin.element.InternalCooldownContext;
import io.github.xrickastley.originsgenshin.element.reaction.ElementalReaction;
import io.github.xrickastley.originsgenshin.element.reaction.ElementalReactions;
import io.github.xrickastley.originsgenshin.registry.OriginsGenshinDamageTypes;
import io.github.xrickastley.originsgenshin.util.ClassInstanceUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Arm;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

// Should technically extend Entity, but extends LivingEntity instead to NOT deal with more Networking and Spawn Packets.
public final class DendroCoreEntity extends LivingEntity {
	private List<LivingEntity> owners;
	private @Nullable LivingEntity target;
	private Type type = Type.NORMAL;
	private boolean exploded = false;
	private int hyperbloomAge = 0;
	private int ageSplit = 10;

	public DendroCoreEntity(EntityType<? extends LivingEntity> entityType, World world) {
		this(entityType, world, null);
	}
	
	public DendroCoreEntity(EntityType<? extends LivingEntity> entityType, World world, @Nullable LivingEntity owner) {
		super(entityType, world);

		this.owners = new ArrayList<>();
		if (owner != null) this.owners.add(owner);
	}

	public static DefaultAttributeContainer.Builder getAttributeBuilder() {
		return LivingEntity.createLivingAttributes()
			.add(EntityAttributes.GENERIC_MAX_HEALTH, 1);
	}

	public int getAge() {
		return this.age;
	}

	public DendroCoreEntity setOwner(LivingEntity owner) {
		this.owners = new ArrayList<>();

		if (owner != null) this.owners.add(owner);

		return this;
	}

	public DendroCoreEntity addOwner(LivingEntity owner) {
		if (owner != null) this.owners.add(owner);

		return this;
	}

	public void setAsHyperbloom() {
		if (this.type != Type.NORMAL) throw new IllegalStateException("This DendroCoreEntity has already been transformed! Type: " + this.type);
		
		this.type = Type.HYPERBLOOM;
		this.hyperbloomAge = this.age;
		this.noClip = true;
		this.setNoGravity(true);

		final @Nullable LivingEntity target = ElementalReaction
			.getEntitiesInAoE(this, 32.0)
			.stream()
			.filter(e -> !this.owners.contains(e) && !(e instanceof DendroCoreEntity))
			.sorted(Comparator.comparing(e -> e.squaredDistanceTo(this)))
			.findFirst()
			.orElse(null);

		this.target = target;

		if (this.target != null) this.ageSplit = Math.max((int) (this.squaredDistanceTo(target) / 24.0), 1);
	}

	public void setAsBurgeon() {
		if (this.type != Type.NORMAL) throw new IllegalStateException("This DendroCoreEntity has already been transformed! Type: " + this.type);

		this.type = Type.BURGEON;
		this.explode(3.0);
	}

	public boolean isNormal() {
		return this.type == Type.NORMAL;
	}

	public boolean isHyperbloom() {
		return this.type == Type.HYPERBLOOM;
	}
	
	public boolean isBurgeon() {
		return this.type == Type.BURGEON;
	}

	// why does it say NPE on target.getBoundingBox :sob:, IT'S ALREADY TARGET != NULL??
	@SuppressWarnings("null")
	public void doHyperbloom() {
		final int hyperbloomTick = this.age - this.hyperbloomAge;

		if (target != null) {
			Vec3d vecToTarget = target.getEyePos().subtract(this.getPos());

            if (hyperbloomTick < ageSplit)
				vecToTarget = vecToTarget.add(0.0, 3, 0.0);
            else
                vecToTarget = vecToTarget.add(0.0, (ageSplit - hyperbloomTick) * 0.1, 0.0);

			if (!this.getWorld().isClient) {
				System.out.println(String.format("vecToTarget: %s | hyperbloomTick: %d", vecToTarget, hyperbloomTick));
				System.out.println(String.format("ageSplit: %d (%.2f)", ageSplit, ageSplit * 2.5));
			}

            super.setVelocity(vecToTarget.normalize().multiply(0.75));

			if (target.getBoundingBox().contains(this.getPos())) {
				this.target.damage(this.createDamageSource(target), ElementalReaction.getReactionDamage(this, 3.0));
				this.remove(RemovalReason.KILLED);
			}

			if (hyperbloomTick >= (ageSplit * 10)) this.remove(RemovalReason.KILLED);
		} else {
			super.setVelocity(new Vec3d(0, 0.5, 0));
			
			if (hyperbloomTick >= 40) this.remove(RemovalReason.KILLED);
		}
	}

	@Override
	public Arm getMainArm() {
		return Arm.LEFT;
	}

	@Override
	public void setPose(EntityPose pose) {
		super.setPose(EntityPose.STANDING);
	}

	@Override
	public EntityPose getPose() {
		return EntityPose.STANDING;
	}

	@Override
	public void setVelocity(Vec3d velocity) {}

	@Override
	public Iterable<ItemStack> getArmorItems() {
		return DefaultedList.of();
	}

	@Override
	public void equipStack(EquipmentSlot slot, ItemStack stack) {
		// You cannot equip a Dendro Core with an item.
	}

	@Override
	public ItemStack getEquippedStack(EquipmentSlot slot) {
		// A Dendro Core has no items.
		return ItemStack.EMPTY;
	}

	@Override
	public boolean canHaveStatusEffect(StatusEffectInstance effect) {
		return false;
	}

	@Override
	public boolean addStatusEffect(StatusEffectInstance effect, Entity source) {
		return false;
	}

	@Override
	public void kill() {
		this.explode(2.0);
		this.remove(RemovalReason.KILLED);
	}

	@Override
	public boolean damage(DamageSource source, float amount) {
		source = ElementComponent.applyElementalInfusions(source, this);

		if (source instanceof final ElementalDamageSource eds && this.isNormal()) {
			final Element element = eds.getElementalApplication().getElement();

			if (element != Element.PYRO && element != Element.ELECTRO) return false;
			
			final ElementalReaction reaction = element == Element.PYRO
				? ElementalReactions.BURGEON
				: ElementalReactions.HYPERBLOOM;

			reaction.trigger(this, ClassInstanceUtil.castOrNull(source.getAttacker(), LivingEntity.class));
		}

		return false;
	}

	@Override
	public boolean collidesWith(Entity other) {
		return false;
	}

	@Override
	public boolean isPushable() {
		return false;
	}

	@Override
	public void pushAwayFrom(Entity entity) {}

	@Override
	public void tick() {
		super.tick();

		if (this.age == 1) this.removeOldDendroCores(); 

		if (this.type == Type.HYPERBLOOM) this.doHyperbloom();

		if (this.age >= 120 && type != Type.HYPERBLOOM) {
			this.explode(2.0);
			this.remove(RemovalReason.KILLED);
		}
	}

	private void removeOldDendroCores() {
		if (this.getWorld().isClient) return;

		final Box box = Box.of(this.getLerpedPos(1f), 24, 24, 24);
		final List<DendroCoreEntity> dendroCores = this.getWorld().getEntitiesByClass(DendroCoreEntity.class, box, dc -> true);

		if (dendroCores.size() <= 5) return;
		
		dendroCores.sort(Comparator.comparing(DendroCoreEntity::getAge).reversed());

		final Queue<DendroCoreEntity> queue = new LinkedList<>(dendroCores);

		while (queue.peek() != null && queue.size() > 5) queue.remove().kill();
	}

	private boolean explode(final double reactionMultiplier) {
		if (this.exploded) return false;

		this.exploded = true;
		this.age = 118;

		final @Nullable LivingEntity recentOwner = owners.isEmpty() ? null : owners.get(owners.size() - 1);
		for (final LivingEntity target : ElementalReaction.getEntitiesInAoE(this, 5.0)) {
			if (target instanceof DendroCoreEntity) continue;
			
			final ElementalDamageSource source = this.createDamageSource(target, recentOwner);

			float damage = ElementalReaction.getReactionDamage(this, reactionMultiplier);

			if (this.owners.contains(target)) damage *= 0.05f;

			target.damage(source, damage);
		}

		return true;
	}

	private ElementalDamageSource createDamageSource(final LivingEntity target) {
		final @Nullable LivingEntity recentOwner = owners.isEmpty() ? null : owners.get(owners.size() - 1);

		return this.createDamageSource(target, recentOwner);
	}

	private ElementalDamageSource createDamageSource(final LivingEntity target, final LivingEntity recentOwner) {
		return new ElementalDamageSource(
			this.getWorld()
				.getDamageSources()
				.create(OriginsGenshinDamageTypes.DENDRO_CORE, this, recentOwner),
			ElementalApplications.gaugeUnits(target, Element.DENDRO, 0.0),
			InternalCooldownContext.ofNone(recentOwner)
		);
	}

	static {
		ElementComponent.denyElementsFor(DendroCoreEntity.class);
	}

	private static enum Type {
		NORMAL, HYPERBLOOM, BURGEON
	}
}