package io.github.xrickastley.originsgenshin;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.apace100.origins.origin.Origin;
import io.github.apace100.origins.registry.ModComponents;
import io.github.xrickastley.originsgenshin.interfaces.IOrigin;
import io.github.xrickastley.originsgenshin.renderer.genshin.ElementalBurstRenderer;
import io.github.xrickastley.originsgenshin.renderer.genshin.ElementalSkillRenderer;
import io.github.xrickastley.originsgenshin.util.ClientConfig;
import io.github.xrickastley.originsgenshin.util.Rescaler;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;

public class OriginsGenshinClient implements ClientModInitializer {
	public static final String MOD_ID = "origins-genshin";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	private static final MinecraftClient client = MinecraftClient.getInstance();
	private static final Rescaler RESCALER = new Rescaler(1920, 1080);
	private static final ElementalBurstRenderer ELEMENTAL_BURST_RENDERER = new ElementalBurstRenderer(RESCALER);
	private static final ElementalSkillRenderer ELEMENTAL_SKILL_RENDERER = new ElementalSkillRenderer(RESCALER);

	@Override
	public void onInitializeClient() {
		OriginsGenshinClient.LOGGER.info("Origins: Genshin (Client) Initialized!");

		HudRenderCallback.EVENT.register(this::renderSkills);

		AutoConfig.register(ClientConfig.class, GsonConfigSerializer::new);
	}

	protected void renderSkills(DrawContext context, float tickDeltaManager) {
		renderElementalBurst(RESCALER, context, Math.max(tickDeltaManager, 0f));
		renderElementalSkill(RESCALER, context, Math.max(tickDeltaManager, 0f));
	}

	protected void renderElementalBurst(Rescaler rescaler, DrawContext context, float tickDeltaManager) {
		for (Origin origin : ModComponents.ORIGIN.get(client.player).getOrigins().values()) {
			IOrigin originMixinData = ((IOrigin)(Object) origin);

			if (originMixinData.originsgenshin$hasElementalBurstPower(client.player)) ELEMENTAL_BURST_RENDERER.setOrPersist(originMixinData.originsgenshin$getElementalBurstPower(client.player));
		}

		ELEMENTAL_BURST_RENDERER.render(context, tickDeltaManager);
	}

	protected void renderElementalSkill(Rescaler rescaler, DrawContext context, float tickDeltaManager) {
		for (Origin origin : ModComponents.ORIGIN.get(client.player).getOrigins().values()) {
			IOrigin originMixinData = ((IOrigin)(Object) origin);

			if (originMixinData.originsgenshin$hasElementalSkillPower(client.player)) ELEMENTAL_SKILL_RENDERER.setOrPersist(originMixinData.originsgenshin$getElementalSkillPower(client.player));
		}

		ELEMENTAL_SKILL_RENDERER.render(context, tickDeltaManager);
	}
}
