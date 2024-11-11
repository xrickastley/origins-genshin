package io.github.xrickastley.originsgenshin.entity;

import javax.annotation.Nullable;

import io.github.xrickastley.originsgenshin.OriginsGenshin;
import io.github.xrickastley.originsgenshin.element.Element;
import io.github.xrickastley.originsgenshin.element.ElementalApplication;
import io.github.xrickastley.originsgenshin.element.ElementalDamageSource;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.damage.DamageTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Arm;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;

// Should technically extend Entity, but extends LivingEntity instead to NOT deal with more Networking and Spawn Packets.
public class DendroCoreEntity extends LivingEntity {
	private static final double radius = 2.5;
	private final @Nullable LivingEntity owner;

	public DendroCoreEntity(EntityType<? extends LivingEntity> entityType, World world) {
		super(entityType, world);

		this.owner = null;
	}
	
	public DendroCoreEntity(EntityType<? extends LivingEntity> entityType, World world, @Nullable LivingEntity owner) {
		super(entityType, world);

		this.owner = owner;
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

	public static DefaultAttributeContainer.Builder getAttributeBuilder() {
		return DefaultAttributeContainer.builder();
	}

	@Override
	public void tick() {
		super.tick();
		
		if (this.age < 120) return;
		
		for (final LivingEntity target : this.getWorld().getNonSpectatingEntities(LivingEntity.class, Box.of(this.getLerpedPos(1F), radius * 2, radius * 2, radius * 2))) {
			final ElementalDamageSource source = new ElementalDamageSource(
				this
					.getWorld()
					.getDamageSources()
					.create(DamageTypes.ARROW, this, owner),
				ElementalApplication.usingGaugeUnits(target, Element.DENDRO, 0.0),
				"dendro_core_expiration_explosion"
			);
			final float damage = 2 * OriginsGenshin.getLevelMultiplier(this);

			target.damage(source, damage);
		}

		this.kill();
	}
}