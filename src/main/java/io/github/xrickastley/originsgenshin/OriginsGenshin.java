package io.github.xrickastley.originsgenshin;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleFactory;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleRegistry;
import net.fabricmc.fabric.api.gamerule.v1.rule.DoubleRule;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.entity.Entity;
import net.minecraft.registry.Registry;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.resource.ResourceType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.util.Identifier;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.apace100.apoli.util.Scheduler;
import io.github.xrickastley.originsgenshin.element.InternalCooldownType;
import io.github.xrickastley.originsgenshin.element.reaction.ElementalReactions;
import io.github.xrickastley.originsgenshin.factory.OriginsGenshinAttributes;
import io.github.xrickastley.originsgenshin.factory.OriginsGenshinBiEntityActions;
import io.github.xrickastley.originsgenshin.factory.OriginsGenshinEntities;
import io.github.xrickastley.originsgenshin.factory.OriginsGenshinStatusEffects;
import io.github.xrickastley.originsgenshin.registry.OriginsGenshinRegistries;
import io.github.xrickastley.originsgenshin.registry.OriginsGenshinRegistryKeys;
import io.github.xrickastley.originsgenshin.registry.OriginsGenshinReloadListener;

public class OriginsGenshin implements ModInitializer {
	public static final String MOD_ID = "origins-genshin";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	public static final Scheduler SCHEDULER = new Scheduler();
	
	public static final GameRules.Key<GameRules.BooleanRule> OVERLOADED_EXPLOSIONS_DAMAGE_BLOCKS = GameRuleRegistry.register(
		"overloadedBlockDestruction", 
		GameRules.Category.PLAYER, 
		GameRuleFactory.createBooleanRule(true)
	);

	public static final GameRules.Key<DoubleRule> LEVEL_MULTIPLIER = GameRuleRegistry.register(
		"levelMultiplier", 
		GameRules.Category.PLAYER, 
		GameRuleFactory.createDoubleRule(1)
	);

	@Override
	public void onInitialize() {
		LOGGER.info("Origins: Genshin Initialized!");

		OriginsGenshinRegistryKeys.load();
		OriginsGenshinRegistries.load();

		OriginsGenshinAttributes.register();
		OriginsGenshinEntities.register();
		OriginsGenshinBiEntityActions.register();
		OriginsGenshinStatusEffects.register();

		ElementalReactions.register();

		ResourceManagerHelper
			.get(ResourceType.SERVER_DATA)
			.registerReloadListener(new OriginsGenshinReloadListener());

		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
			dispatcher.register(
				CommandManager
					.literal("eval")
					.executes(context -> {
						// final DynamicRegistryManager manager = context.getSource().getWorld().getRegistryManager();
						final Registry<InternalCooldownType> internalCooldowns = OriginsGenshinRegistries.INTERNAL_COOLDOWN_TYPE;

						LOGGER.info(internalCooldowns.getClass().getName());
						LOGGER.info("There are currently {} registered entries for Registry<InternalCooldown.Builder>", internalCooldowns.size());

						internalCooldowns
							.streamEntries()
							.map(RegistryEntry.Reference::value)
							.forEach(icd -> LOGGER.info("Registered InternalCooldown: {}", icd));

						return 1;
					})
			);
		});
	}

	public static Identifier identifier(String path) {
		return Identifier.of(MOD_ID, path);
	}
	
	public static Logger sublogger(String sublogger) {
		return LoggerFactory.getLogger(MOD_ID + "/" + sublogger);
	}
		
	public static Logger sublogger(Class<?> sublogger) {
		return LoggerFactory.getLogger(MOD_ID + "/" + sublogger.getSimpleName());
	}
		
	public static Logger sublogger(Object sublogger) {
		return LoggerFactory.getLogger(MOD_ID + "/" + sublogger.getClass().getSimpleName());
	}

	public static float getLevelMultiplier(Entity entity) {
		return getLevelMultiplier(entity.getWorld());
	}

	public static float getLevelMultiplier(World world) {
		return (float) world
			.getGameRules()
			.get(OriginsGenshin.LEVEL_MULTIPLIER)
			.get();
	}
}