package io.github.xrickastley.originsgenshin.mixin;

import java.util.Collection;
import java.util.Set;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import io.github.xrickastley.originsgenshin.networking.SyncBossBarEntityS2CPacket;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.boss.BossBar;
import net.minecraft.entity.boss.ServerBossBar;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;

@Mixin(ServerBossBar.class)
public abstract class ServerBossBarMixin 
	extends BossBar
{
	@Shadow
	@Final
	private Set<ServerPlayerEntity> players;

	@Shadow
	public abstract Collection<ServerPlayerEntity> getPlayers();

	public ServerBossBarMixin(Text displayName, BossBar.Color color, BossBar.Style style) {
		super(MathHelper.randomUuid(), displayName, color, style);

		throw new AssertionError();
	}

	public void originsgenshin$setEntity(LivingEntity entity) {
		super.originsgenshin$setEntity(entity);

		final SyncBossBarEntityS2CPacket packet = new SyncBossBarEntityS2CPacket(this, this.originsgenshin$getEntity());

		this.getPlayers()
			.forEach(player -> ServerPlayNetworking.send(player, packet));
	}

	@Inject(
		method = "addPlayer",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/server/network/ServerPlayNetworkHandler;sendPacket(Lnet/minecraft/network/packet/Packet;)V"
		)
	)
	private void sendEntitySync(ServerPlayerEntity player, CallbackInfo ci) {
		if (this.originsgenshin$getEntity() == null) return;

		final SyncBossBarEntityS2CPacket packet = new SyncBossBarEntityS2CPacket(this, this.originsgenshin$getEntity());

		ServerPlayNetworking.send(player, packet);
	}
}
