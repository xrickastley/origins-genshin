package io.github.xrickastley.originsgenshin.entity;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;

import org.jetbrains.annotations.Nullable;

import io.github.xrickastley.originsgenshin.OriginsGenshin;
import io.github.xrickastley.originsgenshin.component.ElementComponent;
import io.github.xrickastley.originsgenshin.element.Element;
import io.github.xrickastley.originsgenshin.element.reaction.ElementalReaction;
import io.github.xrickastley.originsgenshin.factory.OriginsGenshinSoundEvents;
import io.github.xrickastley.originsgenshin.util.ClassInstanceUtil;
import io.github.xrickastley.originsgenshin.util.JavaScriptUtil;
import net.fabricmc.fabric.api.networking.v1.FabricPacket;
import net.fabricmc.fabric.api.networking.v1.PacketType;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.AnimationState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.world.World;

// Should technically extend Entity, but extends LivingEntity instead to NOT deal with more Networking and Spawn Packets.
public final class CrystallizeShardEntity extends OriginsGenshinEntity {
	public final AnimationState idleAnimationState = new AnimationState();
	private @Nullable Element element;
	private @Nullable UUID owner;

	public CrystallizeShardEntity(EntityType<? extends LivingEntity> entityType, World world) {
		this(entityType, world, null, null);
	}

	public CrystallizeShardEntity(EntityType<? extends LivingEntity> entityType, World world, Element element) {
		this(entityType, world, element, null);
	}

	public CrystallizeShardEntity(EntityType<? extends LivingEntity> entityType, World world, Element element, @Nullable LivingEntity owner) {
		super(entityType, world);

		this.element = this.getWorld().isClient ? null : element;
		this.owner = ClassInstanceUtil.mapOrNull(owner, LivingEntity::getUuid);
	}

	@Override
	public void writeCustomDataToNbt(NbtCompound nbt) {
		super.writeCustomDataToNbt(nbt);

		if (this.element != null) nbt.putString("Element", this.element.toString());

		if (this.owner != null) nbt.putUuid("Owner", this.owner);
	}

	@Override
	public void readCustomDataFromNbt(NbtCompound nbt) {
		super.readCustomDataFromNbt(nbt);

		if (nbt.contains("Element")) this.element = Element.valueOf(nbt.getString("Element"));

		if (nbt.contains("Owner")) this.owner = nbt.getUuid("Owner");
	}

	@Override
	public void tick() {
		super.tick();

    	if (this.getWorld().isClient()) this.idleAnimationState.start(this.age);

		this.checkCrystallizeShield();
		this.syncToPlayers();
	}

	@Override
	public boolean collidesWith(Entity other) {
		return other instanceof CrystallizeShardEntity;
	}

	/**
	 * Gets the element of this {@code CrystallizeShardEntity}. <br> <br>
	 *
	 * This is guaranteed to only be nullable <b>if</b> the world is on the client, as the element
	 * is considered {@code null} until the sync packet is received from the server. <br> <br>
	 *
	 * While the element is considered {@code null}, the Crystallize Shard is not rendered. <br> <br>
	 */
	public @Nullable Element getElement() {
		return this.getWorld().isClient
			? element
			: JavaScriptUtil.nullishCoalesing(element, Element.GEO);
	}

	public void syncFromPacket(SyncCrystallizeShardTypeS2CPacket packet) {
		this.element = packet.element;
	}

	public void syncToPlayers() {
		if (!(this.getWorld() instanceof ServerWorld)) return;

		final SyncCrystallizeShardTypeS2CPacket packet = new SyncCrystallizeShardTypeS2CPacket(this.getId(), this.element);

		for (final ServerPlayerEntity otherPlayer : PlayerLookup.tracking(this))
			ServerPlayNetworking.send(otherPlayer, packet);
	}

	private void checkCrystallizeShield() {
		if (this.getWorld().isClient) return;

		final List<LivingEntity> entities = ElementalReaction.getEntitiesInAoE(this, 1.0, e -> !(e instanceof CrystallizeShardEntity));
		final @Nullable LivingEntity owner = this.getEntityFromUUID(this.owner);

		@Nullable LivingEntity target = null;

		if (this.age > 300) {
			this.remove(RemovalReason.KILLED);
		} else if (this.age <= 150 && entities.contains(owner)) {
			target = owner;
		} else if (this.owner == null || this.age > 150) {
			target = entities
				.stream()
				.sorted(Comparator.comparingDouble(this::distanceTo))
				.findFirst()
				.orElse(null);
		}

		if (target == null) return;

		final ElementComponent component = ElementComponent.KEY.get(target);

		component.setCrystallizeShield(element, OriginsGenshin.getLevelMultiplier(this) * 3);
		
		this.getWorld()
			.playSound(null, this.getBlockPos(), OriginsGenshinSoundEvents.CRYSTALLIZE_SHIELD, SoundCategory.PLAYERS, 1.0f, 1.0f);

		this.remove(RemovalReason.KILLED);
	}

	public static class SyncCrystallizeShardTypeS2CPacket implements FabricPacket {
		public static final PacketType<SyncCrystallizeShardTypeS2CPacket> TYPE = PacketType.create(
			OriginsGenshin.identifier("s2c/sync_crystallize_shard_type"), SyncCrystallizeShardTypeS2CPacket::read
		);

		private final int entityId;
		private final Element element;

		private SyncCrystallizeShardTypeS2CPacket(int entityId, Element element) {
			this.entityId = entityId;
			this.element = element;
		}

		private static SyncCrystallizeShardTypeS2CPacket read(PacketByteBuf buffer) {
			return new SyncCrystallizeShardTypeS2CPacket(buffer.readInt(), Element.valueOf(buffer.readString()));
		}

		@Override
		public void write(PacketByteBuf buffer) {
			buffer.writeInt(entityId);
			buffer.writeString(element.toString());
		}

		@Override
		public PacketType<?> getType() {
			return TYPE;
		}

		public int entityId() {
			return entityId;
		}
	}
}
