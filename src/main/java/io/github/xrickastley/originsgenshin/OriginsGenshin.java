package io.github.xrickastley.originsgenshin;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.ArgumentTypeRegistry;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.command.argument.serialize.ConstantArgumentSerializer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.registry.Registry;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.resource.ResourceType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.apace100.apoli.util.Scheduler;
import io.github.xrickastley.originsgenshin.command.ElementArgumentType;
import io.github.xrickastley.originsgenshin.command.ElementCommand;
import io.github.xrickastley.originsgenshin.component.ElementComponent;
import io.github.xrickastley.originsgenshin.element.Element;
import io.github.xrickastley.originsgenshin.element.InternalCooldownType;
import io.github.xrickastley.originsgenshin.element.reaction.ElementalReactions;
import io.github.xrickastley.originsgenshin.factory.OriginsGenshinAttributes;
import io.github.xrickastley.originsgenshin.factory.OriginsGenshinBiEntityActions;
import io.github.xrickastley.originsgenshin.factory.OriginsGenshinEntities;
import io.github.xrickastley.originsgenshin.factory.OriginsGenshinGameRules;
import io.github.xrickastley.originsgenshin.factory.OriginsGenshinPowers;
import io.github.xrickastley.originsgenshin.factory.OriginsGenshinSoundEvents;
import io.github.xrickastley.originsgenshin.factory.OriginsGenshinStatusEffects;
import io.github.xrickastley.originsgenshin.registry.OriginsGenshinRegistries;
import io.github.xrickastley.originsgenshin.registry.OriginsGenshinRegistryKeys;
import io.github.xrickastley.originsgenshin.registry.OriginsGenshinReloadListener;

public class OriginsGenshin implements ModInitializer {
	// TODO: make mod client/server, not client AND server

	public static final String MOD_ID = "origins-genshin";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	public static final Scheduler SCHEDULER = new Scheduler();

	@Override
	public void onInitialize() {
		LOGGER.info("Origins: Genshin Initialized!");

		OriginsGenshinRegistryKeys.load();
		OriginsGenshinRegistries.load();

		OriginsGenshinAttributes.register();
		OriginsGenshinEntities.register();
		OriginsGenshinBiEntityActions.register();
		OriginsGenshinStatusEffects.register();
		OriginsGenshinPowers.register();
		OriginsGenshinSoundEvents.register();
		OriginsGenshinGameRules.register();

		ElementalReactions.register();

		ResourceManagerHelper
			.get(ResourceType.SERVER_DATA)
			.registerReloadListener(OriginsGenshinReloadListener.INSTANCE);

		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
			dispatcher.register(
				CommandManager
					.literal("eval")
					.then(
						CommandManager
							.argument("entity", EntityArgumentType.entity())
							.then(
								CommandManager
									.argument("element", ElementArgumentType.element())
									.executes(context -> {
										final Entity entity = EntityArgumentType.getEntity(context, "entity");
										final Element element = ElementArgumentType.getElement(context, "element");

										if (!(entity instanceof final LivingEntity livingEntity)) {
											context.getSource().sendError(Text.literal("/eval target must be a LivingEntity!"));
										
											return 0;
										}
									
										final ElementComponent component = ElementComponent.KEY.get(livingEntity);
									
										component.setCrystallizeShield(element, 50);
									
										return 1;
									})
							)
							.executes(context -> {
								final Entity entity = EntityArgumentType.getEntity(context, "entity");

								if (!(entity instanceof final LivingEntity livingEntity)) {
									context.getSource().sendError(Text.literal("/eval target must be a LivingEntity!"));

									return 0;
								}

								final ElementComponent component = ElementComponent.KEY.get(livingEntity);

								component.setCrystallizeShield(Element.HYDRO, 50);

								return 1;
							})
					)
					.executes(context -> {
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

			ElementCommand.register(dispatcher);
		});

		ArgumentTypeRegistry.registerArgumentType(
			OriginsGenshin.identifier("element"),
			ElementArgumentType.class,
			ConstantArgumentSerializer.of(ElementArgumentType::new)
		);
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
			.get(OriginsGenshinGameRules.LEVEL_MULTIPLIER)
			.get();
	}
}