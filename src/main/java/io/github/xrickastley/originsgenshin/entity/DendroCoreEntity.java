package io.github.xrickastley.originsgenshin.entity;

import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import javax.annotation.Nullable;

import io.github.xrickastley.originsgenshin.component.ElementComponent;
import io.github.xrickastley.originsgenshin.element.Element;
import io.github.xrickastley.originsgenshin.element.ElementalApplications;
import io.github.xrickastley.originsgenshin.element.ElementalDamageSource;
import io.github.xrickastley.originsgenshin.element.InternalCooldownContext;
import io.github.xrickastley.originsgenshin.element.reaction.ElementalReaction;
import io.github.xrickastley.originsgenshin.registry.OriginsGenshinDamageTypes;
import net.minecraft.entity.Entity;
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
import net.minecraft.world.World;

// Should technically extend Entity, but extends LivingEntity instead to NOT deal with more Networking and Spawn Packets.
public final class DendroCoreEntity extends LivingEntity {
	private @Nullable LivingEntity owner;

	public DendroCoreEntity(EntityType<? extends LivingEntity> entityType, World world) {
		super(entityType, world);

		this.owner = null;
	}
	
	public DendroCoreEntity(EntityType<? extends LivingEntity> entityType, World world, @Nullable LivingEntity owner) {
		super(entityType, world);

		this.owner = owner;
	}

	public int getAge() {
		return this.age;
	}

	public DendroCoreEntity setOwner(LivingEntity owner) {
		this.owner = owner;

		return this;
	}

	@Override
	public Arm getMainArm() {
		return Arm.LEFT;
	}

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

	public static DefaultAttributeContainer.Builder getAttributeBuilder() {
		return LivingEntity.createLivingAttributes()
			.add(EntityAttributes.GENERIC_MAX_HEALTH, 1);
	}

	@Override
	public void kill() {
		for (final LivingEntity target : ElementalReaction.getEntitiesInAoE(this, 5.0)) {
			float damage = ElementalReaction.getReactionDamage(this, 2.0);
			final ElementalDamageSource source = new ElementalDamageSource(
				this.getWorld()
					.getDamageSources()
					.create(OriginsGenshinDamageTypes.DENDRO_CORE, this, owner),
				ElementalApplications.gaugeUnits(target, Element.DENDRO, 0.0),
				InternalCooldownContext.ofNone(owner)
			);

			if (owner != null && owner == target) damage *= 0.05f;

			target.damage(source, damage);
		}

		super.kill();
	}

	@Override
	public boolean damage(DamageSource source, float amount) {
		if (source != this.getWorld().getDamageSources().genericKill() || amount != Float.MAX_VALUE) return false;
		
		remove(RemovalReason.KILLED);

		return true;
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
		
		if (this.age == 1) removeOldDendroCores(); 

		if (this.age < 120) return;

		this.kill();
	}

	private void removeOldDendroCores() {
		if (this.getWorld().isClient) return;

		final Box box = Box.of(this.getLerpedPos(1f), 24, 24, 24);
		final List<DendroCoreEntity> dendroCores = this.getWorld().getEntitiesByClass(DendroCoreEntity.class, box, dc -> true);

		System.out.println(box);
		System.out.println("Dendro Cores (nearby):  " + dendroCores.size());

		if (dendroCores.size() > 5) {
			dendroCores.sort(Comparator.comparing(DendroCoreEntity::getAge).reversed());

			System.out.println(dendroCores);

			final Queue<DendroCoreEntity> queue = new LinkedList<>(dendroCores);

			while (queue.peek() != null && queue.size() > 5) queue.remove().age = 118;
		}
	}

	static {
		ElementComponent.denyElementsFor(DendroCoreEntity.class);
	}
}