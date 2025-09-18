package io.github.xrickastley.originsgenshin.networking;

import io.github.xrickastley.originsgenshin.OriginsGenshin;

import net.fabricmc.fabric.api.networking.v1.FabricPacket;
import net.fabricmc.fabric.api.networking.v1.PacketType;
import net.minecraft.network.PacketByteBuf;

public record SyncDendroCoreAgeS2CPacket(int entityId, int age) implements FabricPacket {
	public static final PacketType<SyncDendroCoreAgeS2CPacket> TYPE = PacketType.create(
		OriginsGenshin.identifier("s2c/sync_dendro_core_age"), SyncDendroCoreAgeS2CPacket::read
	);

	private static SyncDendroCoreAgeS2CPacket read(PacketByteBuf buffer) {
		return new SyncDendroCoreAgeS2CPacket(buffer.readInt(), buffer.readInt());
	}

	@Override
	public void write(PacketByteBuf buffer) {
		buffer.writeInt(entityId);
		buffer.writeInt(age);
	}

	@Override
	public PacketType<?> getType() {
		return TYPE;
	}
}
