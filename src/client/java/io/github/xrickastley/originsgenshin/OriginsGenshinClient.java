package io.github.xrickastley.originsgenshin;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.apace100.origins.origin.Origin;
import io.github.apace100.origins.registry.ModComponents;

import io.github.xrickastley.originsgenshin.interfaces.IOrigin;
import io.github.xrickastley.originsgenshin.renderer.ElementalBurstRenderer;
import io.github.xrickastley.originsgenshin.renderer.ElementalSkillRenderer;
import io.github.xrickastley.originsgenshin.util.ClientConfig;
import io.github.xrickastley.originsgenshin.util.Rescaler;

import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

public class OriginsGenshinClient implements ClientModInitializer {
	public static final String MOD_ID = "origins-genshin";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	private static final MinecraftClient client = MinecraftClient.getInstance();
	private static final Rescaler rescaler = new Rescaler(1920, 1080);
	private static final ElementalBurstRenderer burstRenderer = new ElementalBurstRenderer(rescaler);
	private static final ElementalSkillRenderer skillRenderer = new ElementalSkillRenderer(rescaler);

	@Override
	public void onInitializeClient() {
		OriginsGenshinClient.LOGGER.info("Origins-Genshin (Client) Initialized!");

		HudRenderCallback.EVENT.register(this::renderSkills);
		
		AutoConfig.register(ClientConfig.class, GsonConfigSerializer::new);
	}

	protected void renderSkills(DrawContext drawContext, float tickDeltaManager) {
		renderElementalBurst(rescaler, drawContext, Math.max(tickDeltaManager, 0f));
		renderElementalSkill(rescaler, drawContext, Math.max(tickDeltaManager, 0f));
	}

	protected void renderElementalBurst(Rescaler rescaler, DrawContext drawContext, float tickDeltaManager) {
		for (Origin origin : ModComponents.ORIGIN.get(client.player).getOrigins().values()) {
			IOrigin originMixinData = ((IOrigin)(Object) origin);
	
			if (originMixinData.hasElementalBurstPower(client.player)) burstRenderer.setOrPersist(originMixinData.getElementalBurstPower(client.player));
		}

		burstRenderer.render(drawContext, tickDeltaManager);
	}

	protected void renderElementalSkill(Rescaler rescaler, DrawContext drawContext, float tickDeltaManager) {
		for (Origin origin : ModComponents.ORIGIN.get(client.player).getOrigins().values()) {
			IOrigin originMixinData = ((IOrigin)(Object) origin);

			if (originMixinData.hasElementalSkillPower(client.player)) skillRenderer.setOrPersist(originMixinData.getElementalSkillPower(client.player));
		}

		skillRenderer.render(drawContext, tickDeltaManager);
	}
}