package io.github.xrickastley.originsgenshin.networking;

import java.util.List;

import io.github.xrickastley.originsgenshin.OriginsGenshin;

import net.fabricmc.fabric.api.networking.v1.FabricPacket;
import net.fabricmc.fabric.api.networking.v1.PacketType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.network.PacketByteBuf;

public record ShowElectroChargeS2CPacket(int mainEntity, int[] otherEntities) implements FabricPacket {
	public ShowElectroChargeS2CPacket(LivingEntity mainEntity, List<LivingEntity> otherEntities) {
		this(mainEntity.getId(), otherEntities.stream().mapToInt(LivingEntity::getId).toArray());
	}

	public static final PacketType<ShowElectroChargeS2CPacket> TYPE = PacketType.create(
		OriginsGenshin.identifier("s2c/show_electro_charged"), ShowElectroChargeS2CPacket::read
	);

	private static ShowElectroChargeS2CPacket read(PacketByteBuf buffer) {
		return new ShowElectroChargeS2CPacket(buffer.readInt(), buffer.readIntArray());
	}

	@Override
	public void write(PacketByteBuf buffer) {
		buffer.writeInt(mainEntity);
		buffer.writeIntArray(otherEntities);
	}

	@Override
	public PacketType<?> getType() {
		return TYPE;
	}
}
