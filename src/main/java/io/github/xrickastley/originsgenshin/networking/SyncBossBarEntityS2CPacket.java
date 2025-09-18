package io.github.xrickastley.originsgenshin.networking;

import java.util.UUID;

import io.github.xrickastley.originsgenshin.OriginsGenshin;

import net.fabricmc.fabric.api.networking.v1.FabricPacket;
import net.fabricmc.fabric.api.networking.v1.PacketType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.boss.BossBar;
import net.minecraft.network.PacketByteBuf;

public record SyncBossBarEntityS2CPacket(UUID uuid, boolean hasEntity, int entityId) implements FabricPacket {
	public SyncBossBarEntityS2CPacket(BossBar bossBar, LivingEntity entity) {
		this(bossBar.getUuid(), entity != null, entity == null ? -1 : entity.getId());
	}

	public static final PacketType<SyncBossBarEntityS2CPacket> TYPE = PacketType.create(
		OriginsGenshin.identifier("s2c/sync_boss_bar_entity"), SyncBossBarEntityS2CPacket::read
	);

	private static SyncBossBarEntityS2CPacket read(PacketByteBuf buffer) {
		return new SyncBossBarEntityS2CPacket(buffer.readUuid(), buffer.readBoolean(), buffer.readInt());
	}

	@Override
	public void write(PacketByteBuf buffer) {
		buffer.writeUuid(uuid);
		buffer.writeBoolean(hasEntity);
		buffer.writeInt(entityId);
	}

	@Override
	public PacketType<?> getType() {
		return TYPE;
	}
}
