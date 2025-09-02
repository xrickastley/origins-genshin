package io.github.xrickastley.originsgenshin;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.apace100.origins.origin.Origin;
import io.github.apace100.origins.registry.ModComponents;
import io.github.xrickastley.originsgenshin.factory.OriginsGenshinEntities;
import io.github.xrickastley.originsgenshin.interfaces.IOrigin;
import io.github.xrickastley.originsgenshin.networking.OriginsGenshinPacketsS2C;
import io.github.xrickastley.originsgenshin.networking.SyncBossBarEntityPacketHandler;
import io.github.xrickastley.originsgenshin.renderer.WorldTextRenderer;
import io.github.xrickastley.originsgenshin.renderer.entity.CrystallizeShardEntityRenderer;
import io.github.xrickastley.originsgenshin.renderer.entity.DendroCoreEntityRenderer;
import io.github.xrickastley.originsgenshin.renderer.entity.model.CrystallizeShardEntityModel;
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
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

import io.github.xrickastley.originsgenshin.renderer.genshin.SpecialEffectsRenderer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;

public class OriginsGenshinClient implements ClientModInitializer {
	public static final String MOD_ID = "origins-genshin";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	
	private static final MinecraftClient client = MinecraftClient.getInstance();
	private static final Rescaler RESCALER = new Rescaler(1920, 1080);
	private static final ElementalBurstRenderer ELEMENTAL_BURST_RENDERER = new ElementalBurstRenderer(RESCALER);
	private static final ElementalSkillRenderer ELEMENTAL_SKILL_RENDERER = new ElementalSkillRenderer(RESCALER);
	private static final SpecialEffectsRenderer SPECIAL_EFFECTS_RENDERER = new SpecialEffectsRenderer();
	public static final WorldTextRenderer WORLD_TEXT_RENDERER = new WorldTextRenderer();
	public static final SyncBossBarEntityPacketHandler SYNC_BOSS_BAR_ENTITY_HANDLER = new SyncBossBarEntityPacketHandler();

	@Override
	public void onInitializeClient() {
		OriginsGenshinClient.LOGGER.info("Origins: Genshin (Client) Initialized!");

		HudRenderCallback.EVENT.register(this::renderSkills);

		WorldRenderEvents.END.register(OriginsGenshinClient.SPECIAL_EFFECTS_RENDERER::render);
		ClientTickEvents.START_WORLD_TICK.register(OriginsGenshinClient.SPECIAL_EFFECTS_RENDERER::tick);
		OriginsGenshinPacketsS2C.registerHandler(OriginsGenshinClient.SPECIAL_EFFECTS_RENDERER);
		OriginsGenshinPacketsS2C.registerHandler(OriginsGenshinClient.SYNC_BOSS_BAR_ENTITY_HANDLER);

		WorldRenderEvents.END.register(OriginsGenshinClient.WORLD_TEXT_RENDERER::render);
		ClientTickEvents.START_WORLD_TICK.register(OriginsGenshinClient.WORLD_TEXT_RENDERER::tick);

		EntityRendererRegistry.register(OriginsGenshinEntities.DENDRO_CORE, DendroCoreEntityRenderer::new);
		EntityRendererRegistry.register(OriginsGenshinEntities.CRYSTALLIZE_SHARD, CrystallizeShardEntityRenderer::new);
		EntityModelLayerRegistry.registerModelLayer(DendroCoreEntityModel.MODEL_LAYER, DendroCoreEntityModel::getTexturedModelData);
		EntityModelLayerRegistry.registerModelLayer(CrystallizeShardEntityModel.MODEL_LAYER, CrystallizeShardEntityModel::getTexturedModelData);

		OriginsGenshinPacketsS2C.register();
		
		AutoConfig.register(ClientConfig.class, GsonConfigSerializer::new);
	}

	protected void renderSkills(DrawContext context, float tickDeltaManager) {
		renderElementalBurst(RESCALER, context, Math.max(tickDeltaManager, 0f));
		renderElementalSkill(RESCALER, context, Math.max(tickDeltaManager, 0f));
	}

	protected void renderElementalBurst(Rescaler rescaler, DrawContext context, float tickDeltaManager) {
		for (Origin origin : ModComponents.ORIGIN.get(client.player).getOrigins().values()) {
			IOrigin originMixinData = ((IOrigin)(Object) origin);
	
			if (originMixinData.hasElementalBurstPower(client.player)) ELEMENTAL_BURST_RENDERER.setOrPersist(originMixinData.getElementalBurstPower(client.player));
		}

		ELEMENTAL_BURST_RENDERER.render(context, tickDeltaManager);
	}

	protected void renderElementalSkill(Rescaler rescaler, DrawContext context, float tickDeltaManager) {
		for (Origin origin : ModComponents.ORIGIN.get(client.player).getOrigins().values()) {
			IOrigin originMixinData = ((IOrigin)(Object) origin);

			if (originMixinData.hasElementalSkillPower(client.player)) ELEMENTAL_SKILL_RENDERER.setOrPersist(originMixinData.getElementalSkillPower(client.player));
		}

		ELEMENTAL_SKILL_RENDERER.render(context, tickDeltaManager);
	}
}