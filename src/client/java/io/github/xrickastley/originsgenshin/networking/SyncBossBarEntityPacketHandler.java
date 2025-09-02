package io.github.xrickastley.originsgenshin.networking;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.jetbrains.annotations.Nullable;

import io.github.xrickastley.originsgenshin.OriginsGenshin;
import io.github.xrickastley.originsgenshin.mixin.client.BossBarHudAccessor;
import io.github.xrickastley.originsgenshin.util.ClassInstanceUtil;

import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.PacketType;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.ClientBossBar;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.LivingEntity;

public class SyncBossBarEntityPacketHandler implements PacketHandler<SyncBossBarEntityS2CPacket> {
	private final Map<UUID, LivingEntity> deferredEntities = new HashMap<>();

	@Override
	public PacketType<SyncBossBarEntityS2CPacket> getType() {
		return SyncBossBarEntityS2CPacket.TYPE;
	}

	@Override
	public void receive(SyncBossBarEntityS2CPacket packet, ClientPlayerEntity player, PacketSender responseSender) {
		OriginsGenshin.sublogger().info("RECEIEVE!");

		final MinecraftClient client = MinecraftClient.getInstance();
		final Map<UUID, ClientBossBar> bossBarMap = ((BossBarHudAccessor) client.inGameHud.getBossBarHud())
			.getBossBars();

		final @Nullable ClientBossBar bossBar = bossBarMap.get(packet.uuid());
		final @Nullable LivingEntity entity = packet.hasEntity()
			? ClassInstanceUtil.castOrNull(client.world.getEntityById(packet.entityId()), LivingEntity.class)
			: null;

		// set to map and call on add action (basically defer)
		if (bossBar == null) {
			if (entity == null) return;

			OriginsGenshin
				.sublogger()
				.warn("Received packet for unknown boss bar! Deferring LivingEntity set for {}", packet.uuid());

			this.deferredEntities.put(packet.uuid(), entity);

			return;
		}

		if (!packet.hasEntity()) {
			bossBar.originsgenshin$setEntity(null);

			return;
		}

		if (entity == null) return;

		bossBar.originsgenshin$setEntity(entity);
	}

	public ClientBossBar setPossibleEntity(ClientBossBar bossBar) {
		bossBar.originsgenshin$setEntity(this.deferredEntities.get(bossBar.getUuid()));

		return bossBar;
	}
}
