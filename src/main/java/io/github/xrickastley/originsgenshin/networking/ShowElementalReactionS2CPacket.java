package io.github.xrickastley.originsgenshin.networking;

import io.github.xrickastley.originsgenshin.OriginsGenshin;

import net.fabricmc.fabric.api.networking.v1.FabricPacket;
import net.fabricmc.fabric.api.networking.v1.PacketType;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

public record ShowElementalReactionS2CPacket(int entityId, Identifier reaction) implements FabricPacket {
    public static final PacketType<ShowElementalReactionS2CPacket> TYPE = PacketType.create(
        OriginsGenshin.identifier("s2c/show_elemental_reaction"), ShowElementalReactionS2CPacket::read
    );

    private static ShowElementalReactionS2CPacket read(PacketByteBuf buffer) {
        return new ShowElementalReactionS2CPacket(buffer.readVarInt(), buffer.readIdentifier());
    }

    @Override
    public void write(PacketByteBuf buffer) {
        buffer.writeVarInt(entityId);
        buffer.writeIdentifier(reaction);
    }

    @Override
    public PacketType<?> getType() {
        return TYPE;
    }
}