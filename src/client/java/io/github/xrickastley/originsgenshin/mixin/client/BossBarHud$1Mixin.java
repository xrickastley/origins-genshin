package io.github.xrickastley.originsgenshin.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import io.github.xrickastley.originsgenshin.OriginsGenshinClient;
import io.github.xrickastley.originsgenshin.util.ClassInstanceUtil;

@Mixin(targets={"net.minecraft.client.gui.hud.BossBarHud$1"})
public class BossBarHud$1Mixin {
	@ModifyArg(
		method = "Lnet/minecraft/client/gui/hud/BossBarHud$1;add(Ljava/util/UUID;Lnet/minecraft/text/Text;FLnet/minecraft/entity/boss/BossBar$Color;Lnet/minecraft/entity/boss/BossBar$Style;ZZZ)V",
		at = @At(
			value = "INVOKE",
			target = "Ljava/util/Map;put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;"
		),
		index = 1
	)
	private Object loadEntityIfExistent(Object obj) {
		return OriginsGenshinClient.SYNC_BOSS_BAR_ENTITY_HANDLER.setPossibleEntity(ClassInstanceUtil.cast(obj));
	}
}
