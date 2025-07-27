package io.github.xrickastley.originsgenshin.networking;

import io.github.xrickastley.originsgenshin.OriginsGenshin;
import io.github.xrickastley.originsgenshin.element.Element;

import net.fabricmc.fabric.api.networking.v1.FabricPacket;
import net.fabricmc.fabric.api.networking.v1.PacketType;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.math.Vec3d;

public record ShowElementalDamageS2CPacket(Vec3d pos, Element element, float amount) implements FabricPacket {
	public static final PacketType<ShowElementalDamageS2CPacket> TYPE = PacketType.create(
		OriginsGenshin.identifier("s2c/show_elemental_damage"), ShowElementalDamageS2CPacket::read
	);

	private static ShowElementalDamageS2CPacket read(PacketByteBuf buffer) {
		return new ShowElementalDamageS2CPacket(buffer.readVec3d(), Element.valueOf(buffer.readString()), buffer.readFloat());
	}

	@Override
	public void write(PacketByteBuf buffer) {
		buffer.writeVec3d(pos);
		buffer.writeString(element != null ? element.toString() : "NONE");
		buffer.writeFloat(amount);
	}

	@Override
	public PacketType<?> getType() {
		return TYPE;
	}
}