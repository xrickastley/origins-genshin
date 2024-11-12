package io.github.xrickastley.originsgenshin.networking;

import io.github.xrickastley.originsgenshin.element.reaction.ElementalReaction;
import io.github.xrickastley.originsgenshin.factory.OriginsGenshinParticleFactory;
import io.github.xrickastley.originsgenshin.registry.OriginsGenshinRegistries;
import io.github.xrickastley.originsgenshin.util.Color;
import io.github.xrickastley.originsgenshin.util.Colors;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;

public class OriginsGenshinPacketsS2C {
	public static void register() {
		ClientPlayConnectionEvents.INIT.register(((clientPlayNetworkHandler, minecraftClient) -> {
			ClientPlayNetworking.registerReceiver(ShowElementalReactionS2CPacket.TYPE, OriginsGenshinPacketsS2C::onElementalReactionShow);
			ClientPlayNetworking.registerReceiver(ShowElementalDamageS2CPacket.TYPE, OriginsGenshinPacketsS2C::onElementalDamageShow);
		}));
	}
	
	@SuppressWarnings("resource")
	protected static void onElementalReactionShow(ShowElementalReactionS2CPacket packet, ClientPlayerEntity player, PacketSender responseSender) {
		Entity entity = player.networkHandler.getWorld().getEntityById(packet.entityId());
 
		final ElementalReaction reaction = OriginsGenshinRegistries.ELEMENTAL_REACTION.get(packet.reaction());
 
		if (reaction == null || reaction.getParticle() == null) return;

		MinecraftClient
			.getInstance()
			.player
			.getWorld()
			.addImportantParticle(reaction.getParticle(), entity.getX(), entity.getY() + entity.getHeight(), entity.getZ(), 0.02, 0.02, 0.02);
	}
	
	@SuppressWarnings("resource")
	protected static void onElementalDamageShow(ShowElementalDamageS2CPacket packet, ClientPlayerEntity player, PacketSender responseSender) {
		Entity entity = player.networkHandler.getWorld().getEntityById(packet.entityId());

		if (!(entity instanceof LivingEntity)) return;

		final Color color = packet.element() != null && packet.element().hasDamageColor()
			? packet.element().getDamageColor()
			: Colors.PHYSICAL;

		MinecraftClient
			.getInstance()
			.player
			.getWorld()
			.addImportantParticle(OriginsGenshinParticleFactory.DAMAGE_TEXT, entity.getX(), entity.getY() + (entity.getHeight() * (1 - Math.min(Math.random(), 0.5))), entity.getZ(), packet.amount(), color.asARGB(), 1.0f);
	}
}
