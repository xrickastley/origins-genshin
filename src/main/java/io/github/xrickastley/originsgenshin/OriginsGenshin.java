package io.github.xrickastley.originsgenshin;

import com.mojang.serialization.DynamicOps;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.xrickastley.originsgenshin.integration.ModIntegration;
import io.github.xrickastley.sevenelements.SevenElements;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents;
import net.minecraft.entity.Entity;
import net.minecraft.registry.RegistryOps;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.Identifier;

public class OriginsGenshin implements ModInitializer {
	public static final String MOD_ID = "origins-genshin";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	private static RegistryWrapper.WrapperLookup REGISTRY_WRAPPER;

	@Override
	public void onInitialize() {
		LOGGER.info("Origins: Genshin Initialized!");

		OriginsGenshin.captureRegistryWrapper();

		ModIntegration.loadIntegrations();
	}

	public static Identifier identifier(String path) {
		return Identifier.of(MOD_ID, path);
	}

	public static Logger sublogger() {
		final String className = Thread.currentThread().getStackTrace()[2].getClassName();

		return OriginsGenshin.sublogger(className.substring(className.lastIndexOf(".") + 1));
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
		return SevenElements.getLevelMultiplier(entity.getWorld());
	}

	public static <T> DynamicOps<T> attemptRegistryWrap(DynamicOps<T> ops) {
		return OriginsGenshin.REGISTRY_WRAPPER != null
			? RegistryOps.of(ops, OriginsGenshin.REGISTRY_WRAPPER)
			: ops;
	}

	// really bad impl. but I'm gonna be honest I dunno any other way
	private static void captureRegistryWrapper() {
		ServerWorldEvents.LOAD.register((server, world) -> {
			OriginsGenshin.REGISTRY_WRAPPER = world.getRegistryManager();
		});
	}

	static {
		ModIntegration.registerIntegration("io.github.xrickastley.originsgenshin.integration.SevenElementsIntegration", "seven-elements");
	}
}
