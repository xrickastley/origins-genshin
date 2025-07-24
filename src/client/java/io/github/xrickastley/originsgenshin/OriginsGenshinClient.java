package io.github.xrickastley.originsgenshin;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.apace100.origins.origin.Origin;
import io.github.apace100.origins.registry.ModComponents;
import io.github.xrickastley.originsgenshin.factory.OriginsGenshinEntities;
import io.github.xrickastley.originsgenshin.interfaces.IOrigin;
import io.github.xrickastley.originsgenshin.networking.OriginsGenshinPacketsS2C;
import io.github.xrickastley.originsgenshin.particle.ClientParticleFactory;
import io.github.xrickastley.originsgenshin.renderer.entity.DendroCoreEntityRenderer;
import io.github.xrickastley.originsgenshin.renderer.entity.model.DendroCoreEntityModel;
import io.github.xrickastley.originsgenshin.renderer.genshin.ElementalBurstRenderer;
import io.github.xrickastley.originsgenshin.renderer.genshin.ElementalSkillRenderer;
import io.github.xrickastley.originsgenshin.util.ClientConfig;
import io.github.xrickastley.originsgenshin.util.Rescaler;

import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
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
		OriginsGenshinClient.LOGGER.info("Origins: Genshin (Client) Initialized!");

		HudRenderCallback.EVENT.register(this::renderSkills);

		ClientParticleFactory.register();
		EntityRendererRegistry.register(OriginsGenshinEntities.DENDRO_CORE, DendroCoreEntityRenderer::new);
		EntityModelLayerRegistry.registerModelLayer(DendroCoreEntityModel.MODEL_LAYER, DendroCoreEntityModel::getTexturedModelData);
		OriginsGenshinPacketsS2C.register();
		
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