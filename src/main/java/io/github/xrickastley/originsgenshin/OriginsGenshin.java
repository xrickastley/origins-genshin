package io.github.xrickastley.originsgenshin;

import java.util.function.Consumer;

import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.power.Power;
import io.github.xrickastley.originsgenshin.command.BossBarCommand;
import io.github.xrickastley.originsgenshin.command.ElementArgumentType;
import io.github.xrickastley.originsgenshin.command.ElementCommand;
import io.github.xrickastley.originsgenshin.command.InternalCooldownTagType;
import io.github.xrickastley.originsgenshin.element.Element;
import io.github.xrickastley.originsgenshin.element.ElementalApplication;
import io.github.xrickastley.originsgenshin.events.ElementEvents;
import io.github.xrickastley.originsgenshin.events.ReactionTriggered;
import io.github.xrickastley.originsgenshin.factory.OriginsGenshinFactories;
import io.github.xrickastley.originsgenshin.factory.OriginsGenshinGameRules;
import io.github.xrickastley.originsgenshin.power.ActionOnElementAppliedPower;
import io.github.xrickastley.originsgenshin.power.ActionOnElementEventPower;
import io.github.xrickastley.originsgenshin.power.ActionOnElementRefreshedPower;
import io.github.xrickastley.originsgenshin.power.ActionOnElementRemovedPower;
import io.github.xrickastley.originsgenshin.power.ActionOnElementalReactionPower;
import io.github.xrickastley.originsgenshin.registry.OriginsGenshinReloadListener;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.ArgumentTypeRegistry;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.command.argument.serialize.ConstantArgumentSerializer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

public class OriginsGenshin implements ModInitializer {
	public static final String MOD_ID = "origins-genshin";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		LOGGER.info("Origins: Genshin Initialized!");

		OriginsGenshinFactories.registerAll();

		ResourceManagerHelper
			.get(ResourceType.SERVER_DATA)
			.registerReloadListener(OriginsGenshinReloadListener.INSTANCE);

		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
			BossBarCommand.register(dispatcher);
			ElementCommand.register(dispatcher, registryAccess);
		});

		ArgumentTypeRegistry.registerArgumentType(
			OriginsGenshin.identifier("element"),
			ElementArgumentType.class,
			ConstantArgumentSerializer.of(ElementArgumentType::new)
		);

		ArgumentTypeRegistry.registerArgumentType(
			OriginsGenshin.identifier("internal_cooldown_tag"),
			InternalCooldownTagType.class,
			ConstantArgumentSerializer.of(InternalCooldownTagType::tag)
		);

		ElementEvents.APPLIED
			.register((element, application) -> OriginsGenshin.callElementEventActions(ActionOnElementAppliedPower.class, element, application));

		ElementEvents.REFRESHED
			.register((element, oldApp, newApp) -> OriginsGenshin.callElementEventActions(ActionOnElementRefreshedPower.class, element, newApp));

		ElementEvents.REMOVED
			.register((element, application) -> OriginsGenshin.callElementEventActions(ActionOnElementRemovedPower.class, element, application));

		ReactionTriggered.EVENT
			.register((reaction, reducedGauge, target, origin) ->
				OriginsGenshin.callEventActions(ActionOnElementalReactionPower.class, target, power -> power.trigger(reaction, target, origin))
			);
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
		return getLevelMultiplier(entity.getWorld());
	}

	public static float getLevelMultiplier(World world) {
		return (float) world
			.getGameRules()
			.get(OriginsGenshinGameRules.LEVEL_MULTIPLIER)
			.get();
	}

	private static <T extends ActionOnElementEventPower> void callElementEventActions(Class<T> eventClass, Element element, @Nullable ElementalApplication application) {
		if (application == null) return;

		final LivingEntity entity = application.getEntity();

		OriginsGenshin.callEventActions(eventClass, entity, power -> power.trigger(element, entity));
	}

	private static <T extends Power> void callEventActions(Class<T> eventClass, LivingEntity entity, Consumer<T> powerConsumer) {
		PowerHolderComponent
			.getPowers(entity, eventClass)
			.forEach(powerConsumer);
	}
}
