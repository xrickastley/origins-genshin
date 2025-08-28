package io.github.xrickastley.originsgenshin.networking;

import java.util.ArrayList;
import java.util.List;

import io.github.xrickastley.originsgenshin.OriginsGenshinClient;
import io.github.xrickastley.originsgenshin.element.reaction.ElementalReaction;
import io.github.xrickastley.originsgenshin.entity.DendroCoreEntity;
import io.github.xrickastley.originsgenshin.registry.OriginsGenshinRegistries;
import io.github.xrickastley.originsgenshin.renderer.WorldTextRenderer.DamageText;
import io.github.xrickastley.originsgenshin.renderer.WorldTextRenderer.ReactionText;
import io.github.xrickastley.originsgenshin.util.ClassInstanceUtil;
import io.github.xrickastley.originsgenshin.util.ClientConfig;
import io.github.xrickastley.originsgenshin.util.Color;
import io.github.xrickastley.originsgenshin.util.Colors;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.FabricPacket;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import me.shedaniel.autoconfig.AutoConfig;

public class OriginsGenshinPacketsS2C {
	private static final List<PacketHandler<? extends FabricPacket>> HANDLERS = new ArrayList<>();
	private static boolean registered = false;

	public static void register() {
		ClientPlayConnectionEvents.INIT.register(((clientPlayNetworkHandler, minecraftClient) -> {
			ClientPlayNetworking.registerReceiver(ShowElementalReactionS2CPacket.TYPE, OriginsGenshinPacketsS2C::onElementalReactionShow);
			ClientPlayNetworking.registerReceiver(ShowElementalDamageS2CPacket.TYPE, OriginsGenshinPacketsS2C::onElementalDamageShow);
			ClientPlayNetworking.registerReceiver(SyncDendroCoreAgeS2CPacket.TYPE, OriginsGenshinPacketsS2C::onSyncDendroCoreAge);

			OriginsGenshinPacketsS2C.registerHandlers();
		}));
	}

	public static void registerHandler(final PacketHandler<? extends FabricPacket> handler) {
		if (registered) throw new IllegalStateException("All ClientPlayConnectionEvents.INIT handlers have already been registered!");

		OriginsGenshinPacketsS2C.HANDLERS.add(handler);
	}

	private static void registerHandlers() {
		registered = true;

		for (final PacketHandler<? extends FabricPacket> handler : OriginsGenshinPacketsS2C.HANDLERS)
			ClientPlayNetworking.registerReceiver(handler.getType(), ClassInstanceUtil.cast(handler));
	}

	private static void onElementalReactionShow(ShowElementalReactionS2CPacket packet, ClientPlayerEntity player, PacketSender responseSender) {
		final Vec3d pos = packet.pos();

		final ElementalReaction reaction = OriginsGenshinRegistries.ELEMENTAL_REACTION.get(packet.reaction());

		if (reaction == null || reaction.getText() == null) return;

		OriginsGenshinClient.WORLD_TEXT_RENDERER.addEntry(
			new ReactionText(pos.x, pos.y, pos.z, Colors.PHYSICAL, reaction.getText())
		);
	}

	private static void onElementalDamageShow(ShowElementalDamageS2CPacket packet, ClientPlayerEntity player, PacketSender responseSender) {
		final ClientConfig config = AutoConfig
			.getConfigHolder(ClientConfig.class)
			.getConfig();

		if (!config.renderers.showDamageText) return;

		final Vec3d pos = packet.pos();
		final Color color = packet.element() != null && packet.element().hasDamageColor()
			? packet.element().getDamageColor()
			: Colors.PHYSICAL;
		final float amount = config.developer.genshinDamageLim
			? Math.min(packet.amount(), 20_000_000)
			: packet.amount();

		if (amount == Float.MAX_VALUE) return;

		OriginsGenshinClient.WORLD_TEXT_RENDERER.addEntry(
			new DamageText(pos.x, pos.y, pos.z, color, amount, packet.crit() ? config.renderers.critDMGScale : config.renderers.normalDMGScale)
		);
	}

	private static void onSyncDendroCoreAge(SyncDendroCoreAgeS2CPacket packet, ClientPlayerEntity player, PacketSender responseSender) {
		final World world = MinecraftClient
			.getInstance()
			.player
			.getWorld();

		final Entity entity = world.getEntityById(packet.entityId());

		if (entity == null || !(entity instanceof final DendroCoreEntity dendroCore)) return;

		dendroCore.age = packet.age();
	}
}
