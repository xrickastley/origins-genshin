package io.github.xrickastley.originsgenshin.networking;

import io.github.xrickastley.originsgenshin.OriginsGenshin;

import net.fabricmc.fabric.api.networking.v1.FabricPacket;
import net.fabricmc.fabric.api.networking.v1.PacketType;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;

public record ShowElementalReactionS2CPacket(Vec3d pos, Identifier reaction) implements FabricPacket {
	public static final PacketType<ShowElementalReactionS2CPacket> TYPE = PacketType.create(
		OriginsGenshin.identifier("s2c/show_elemental_reaction"), ShowElementalReactionS2CPacket::read
	);

	private static ShowElementalReactionS2CPacket read(PacketByteBuf buffer) {
		return new ShowElementalReactionS2CPacket(buffer.readVec3d(), buffer.readIdentifier());
	}

	@Override
	public void write(PacketByteBuf buffer) {
		buffer.writeVec3d(pos);
		buffer.writeIdentifier(reaction);
	}

	@Override
	public PacketType<?> getType() {
		return TYPE;
	}
}