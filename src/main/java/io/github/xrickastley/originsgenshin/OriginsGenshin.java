package io.github.xrickastley.originsgenshin;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleFactory;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleRegistry;
import net.fabricmc.fabric.api.gamerule.v1.rule.DoubleRule;
import net.minecraft.entity.Entity;
import net.minecraft.util.Identifier;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.apace100.apoli.util.Scheduler;
import io.github.xrickastley.originsgenshin.element.reaction.ElementalReactions;
import io.github.xrickastley.originsgenshin.factory.OriginsGenshinAttributes;
import io.github.xrickastley.originsgenshin.factory.OriginsGenshinBiEntityActions;
import io.github.xrickastley.originsgenshin.factory.OriginsGenshinEntities;
import io.github.xrickastley.originsgenshin.factory.OriginsGenshinStatusEffects;
import io.github.xrickastley.originsgenshin.registry.OriginsGenshinRegistries;
import io.github.xrickastley.originsgenshin.registry.OriginsGenshinRegistryKeys;

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