package io.github.xrickastley.originsgenshin.networking;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking.PlayPacketHandler;
import net.fabricmc.fabric.api.networking.v1.FabricPacket;
import net.fabricmc.fabric.api.networking.v1.PacketType;

public interface PacketHandler<T extends FabricPacket> extends PlayPacketHandler<T> {
	PacketType<T> getType();
}
