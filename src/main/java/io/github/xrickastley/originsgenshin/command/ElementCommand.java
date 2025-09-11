package io.github.xrickastley.originsgenshin.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import java.util.List;
import java.util.function.Function;

import org.jetbrains.annotations.Nullable;

import io.github.xrickastley.originsgenshin.component.ElementComponent;
import io.github.xrickastley.originsgenshin.component.ElementalInfusionComponent;
import io.github.xrickastley.originsgenshin.element.Element;
import io.github.xrickastley.originsgenshin.element.ElementHolder;
import io.github.xrickastley.originsgenshin.element.ElementalApplication;
import io.github.xrickastley.originsgenshin.element.ElementalApplications;
import io.github.xrickastley.originsgenshin.element.InternalCooldownContext;
import io.github.xrickastley.originsgenshin.element.InternalCooldownTag;
import io.github.xrickastley.originsgenshin.element.InternalCooldownType;
import io.github.xrickastley.originsgenshin.element.reaction.ElementalReaction;
import io.github.xrickastley.originsgenshin.registry.OriginsGenshinRegistryKeys;
import io.github.xrickastley.originsgenshin.util.Array;
import io.github.xrickastley.originsgenshin.util.ClassInstanceUtil;
import io.github.xrickastley.originsgenshin.util.Functions;
import io.github.xrickastley.originsgenshin.util.JavaScriptUtil;

import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.command.argument.RegistryEntryArgumentType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ToolItem;
import net.minecraft.registry.entry.RegistryEntry.Reference;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.text.Texts;
import net.minecraft.util.Identifier;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class ElementCommand {
	public static void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess registryAccess) {
		dispatcher.register(
			CommandManager
				.literal("element")
				.requires(cs -> cs.hasPermissionLevel(2))
				.then(
					literal("apply")
					.then(
						argument("target", EntityArgumentType.entity())
						.then(
							argument("element", ElementArgumentType.element())
							.then(
								argument("gaugeUnits", DoubleArgumentType.doubleArg(0))
								.then(
									literal("gaugeUnit")
									.then(
										argument("isAura", BoolArgumentType.bool())
										.executes(ElementCommand::applyGaugeUnit)
									)
									.executes(ElementCommand::applyGaugeUnit)
								)
								.then(
									literal("duration")
									.then(
										argument("duration", IntegerArgumentType.integer(0))
										.executes(ElementCommand::applyDuration)
									)
								)
								.executes(ElementCommand::applyGaugeUnit)
							)
						)
					)
				)
				.then(
					literal("remove")
					.then(
						argument("target", EntityArgumentType.entity())
						.then(
							argument("element", ElementArgumentType.element())
							.executes(ElementCommand::removeElement)
						)
					)
				)
				.then(
					literal("reduce")
					.then(
						argument("target", EntityArgumentType.entity())
						.then(
							argument("element", ElementArgumentType.element())
							.then(
								argument("gaugeUnits", DoubleArgumentType.doubleArg(0))
								.executes(ElementCommand::reduceElement)
							)
						)
					)
				)
				.then(
					literal("query")
					.then(
						argument("target", EntityArgumentType.entity())
						.then(
							argument("element", ElementArgumentType.element())
							.executes(ElementCommand::queryElement)
						)
						.executes(ElementCommand::queryElements)
					)
				)
				.then(
					literal("infusion")
					.then(
						literal("apply")
						.then(
							argument("element", ElementArgumentType.element())
							.then(
								argument("gaugeUnits", DoubleArgumentType.doubleArg(0))
								.then(
									literal("gaugeUnit")
									.then(
										argument("tag", InternalCooldownTagType.tag())
										.then(
											argument("type", RegistryEntryArgumentType.registryEntry(registryAccess, OriginsGenshinRegistryKeys.INTERNAL_COOLDOWN_TYPE))
											.executes(ElementCommand::infuseGaugeUnit)
										)
										.executes(ElementCommand::infuseGaugeUnit)
									)
									.executes(ElementCommand::infuseGaugeUnit)
								)
								.then(
									literal("duration")
									.then(
										argument("duration", IntegerArgumentType.integer(0))
										.then(
											argument("tag", InternalCooldownTagType.tag())
											.then(
												argument("type", RegistryEntryArgumentType.registryEntry(registryAccess, OriginsGenshinRegistryKeys.INTERNAL_COOLDOWN_TYPE))
												.executes(ElementCommand::infuseDuration)
											)
											.executes(ElementCommand::infuseDuration)
										)
										.executes(ElementCommand::infuseDuration)
									)
								)
								.executes(ElementCommand::infuseGaugeUnit)
							)
						)
					)
					.then(
						literal("remove")
						.executes(ElementCommand::infuseRemove)
					)
				)
		);
	}

	private static final Function<ElementalApplication, String> TO_FRIENDLY_STRING = a -> {
		final String element = a.getElement().toString().substring(0, 1).toUpperCase() + a.getElement().toString().substring(1).toLowerCase();

		return a.isDuration()
			? String.format("%.3fU %s (%.2fs left)", a.getCurrentGauge(), element, a.getRemainingTicks() / 20.0)
			: String.format("%.3fU %s", a.getCurrentGauge(), element);
	};

	private static int applyGaugeUnit(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
		final Entity entity = EntityArgumentType.getEntity(context, "target");
		final Element element = ElementArgumentType.getElement(context, "element");
		final double gaugeUnits = DoubleArgumentType.getDouble(context, "gaugeUnits");
		final boolean aura = ElementCommand.getOrDefault(context, "isAura", Boolean.class, true);

		if (!(entity instanceof final LivingEntity target)) {
			context
				.getSource()
				.sendError(Text.translatable("commands.element.failed.entity", entity));

			return 0;
		}

		final ElementComponent component = ElementComponent.KEY.get(target);
		final List<ElementalReaction> reactions = component
			.addElementalApplication(
				ElementalApplications.gaugeUnits(target, element, gaugeUnits, aura),
				InternalCooldownContext.ofNone()
			);

		context
			.getSource()
			.sendFeedback(
				() -> Text.translatable(reactions.isEmpty() ? "commands.element.apply" : "commands.element.apply.reactions", element, entity.getDisplayName(), Texts.join(reactions, Functions.compose(ElementalReaction::getId, Identifier::toString, Text::literal))),
				true
			);

		return 1;
	}

	private static int applyDuration(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
		final Entity entity = EntityArgumentType.getEntity(context, "target");
		final Element element = ElementArgumentType.getElement(context, "element");
		final double gaugeUnits = DoubleArgumentType.getDouble(context, "gaugeUnits");
		final int duration = IntegerArgumentType.getInteger(context, "duration");

		if (!(entity instanceof final LivingEntity target)) {
			context
				.getSource()
				.sendError(Text.translatable("commands.element.failed.entity", entity));

			return 0;
		}

		final ElementComponent component = ElementComponent.KEY.get(target);
		final List<ElementalReaction> reactions = component
			.addElementalApplication(
				ElementalApplications.duration(target, element, gaugeUnits, duration),
				InternalCooldownContext.ofNone()
			);

		context
			.getSource()
			.sendFeedback(
				() -> Text.translatable(reactions.isEmpty() ? "commands.element.apply" : "commands.element.apply.reactions", element, entity.getDisplayName(), Texts.join(reactions, Functions.compose(ElementalReaction::getId, Identifier::toString, Text::literal))),
				true
			);

		return 1;
	}

	private static int removeElement(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
		final Entity entity = EntityArgumentType.getEntity(context, "target");
		final Element element = ElementArgumentType.getElement(context, "element");

		if (!(entity instanceof final LivingEntity target)) {
			context
				.getSource()
				.sendError(Text.translatable("commands.element.failed.entity", entity));

			return 0;
		}

		final ElementComponent component = ElementComponent.KEY.get(target);
		final ElementHolder holder = component.getElementHolder(element);

		if (!holder.hasElementalApplication()) {
			context
				.getSource()
				.sendError(Text.translatable("commands.element.failed.none", entity.getDisplayName(), element));

			return 0;
		}

		holder.setElementalApplication(null);

		ElementComponent.sync(entity);

		context
			.getSource()
			.sendFeedback(
				() -> Text.translatable("commands.element.remove", element, entity),
				true
			);

		return 1;
	}

	private static int reduceElement(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
		final Entity entity = EntityArgumentType.getEntity(context, "target");
		final Element element = ElementArgumentType.getElement(context, "element");
		final double gaugeUnits = DoubleArgumentType.getDouble(context, "gaugeUnits");

		if (!(entity instanceof final LivingEntity target)) {
			context
				.getSource()
				.sendError(Text.translatable("commands.element.failed.entity", entity));

			return 0;
		}

		final ElementComponent component = ElementComponent.KEY.get(target);
		final ElementHolder holder = component.getElementHolder(element);

		if (!holder.hasElementalApplication())
			return sendError(context, Text.translatable("commands.element.failed.none", entity.getDisplayName(), element));

		final double reducedGauge = holder
			.getElementalApplication()
			.reduceGauge(gaugeUnits);

		ElementComponent.sync(entity);

		return sendFeedback(context, Text.translatable("commands.element.reduce", element, reducedGauge), true);
	}

	private static int queryElements(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
		final Entity entity = EntityArgumentType.getEntity(context, "target");

		if (!(entity instanceof final LivingEntity target))
			return sendError(context, Text.translatable("commands.element.failed.entity", entity));

		final ElementComponent component = ElementComponent.KEY.get(target);
		final Array<ElementalApplication> appliedElements = component.getAppliedElements();

		if (appliedElements.isEmpty())
			return sendError(context, Text.translatable("commands.element.query.multiple.none", entity));
		
		return sendFeedback(context, Text.translatable("commands.element.query.multiple.success", entity.getDisplayName(), Texts.join(appliedElements, Functions.compose(ElementCommand.TO_FRIENDLY_STRING, Text::literal))), true);
	}

	private static int queryElement(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
		final Entity entity = EntityArgumentType.getEntity(context, "target");
		final Element element = ElementArgumentType.getElement(context, "element");

		if (!(entity instanceof final LivingEntity target))
			return sendError(context, Text.translatable("commands.element.failed.entity", entity));

		final ElementComponent component = ElementComponent.KEY.get(target);
		final @Nullable ElementalApplication application = component.getElementHolder(element).getElementalApplication();

		if (application == null)
			return sendError(context, Text.translatable("commands.element.query.single.none", entity.getDisplayName(), element));

		return sendFeedback(context, Text.translatable("commands.element.query.single.success", entity.getDisplayName(), ElementCommand.TO_FRIENDLY_STRING.apply(application)), true);
	}

	private static int infuseGaugeUnit(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
		final Element element = ElementArgumentType.getElement(context, "element");
		final double gaugeUnits = DoubleArgumentType.getDouble(context, "gaugeUnits");
		final InternalCooldownTag tag = InternalCooldownTagType.getTagOrDefault(context, "tag", InternalCooldownTag.NONE);

		final @Nullable Reference<InternalCooldownType> typeRef = ClassInstanceUtil.cast(ElementCommand.getOrDefault(context, "type", Reference.class, null));
		final InternalCooldownType type = JavaScriptUtil.nullishCoalesing(
			ClassInstanceUtil.mapOrNull(typeRef, Reference::value),
			InternalCooldownType.DEFAULT
		);

		final Entity entity = context.getSource().getEntityOrThrow();

		if (!(entity instanceof final LivingEntity livingEntity))
			return sendError(context, Text.translatable("commands.enchant.failed.entity", entity.getName().getString()));

		final ItemStack stack = livingEntity.getMainHandStack();

		if (stack.isEmpty())
			return sendError(context, Text.translatable("commands.enchant.failed.itemless", entity.getName().getString()));

		if (!(stack.getItem() instanceof ToolItem))
			return sendError(context, Text.translatable("commands.element.infuse.failed.incompatible", entity.getName().getString()));

		final ElementalApplication.Builder infusionBuilder = ElementalApplications.builder()
			.setType(ElementalApplication.Type.GAUGE_UNIT)
			.setElement(element)
			.setGaugeUnits(gaugeUnits)
			.setAsAura(false);

		final InternalCooldownContext.Builder icdBuilder = InternalCooldownContext.builder()
			.setTag(tag)
			.setType(type);

		if (ElementalInfusionComponent.applyInfusion(stack, infusionBuilder, icdBuilder)) {
			final String elementString = ElementCommand.TO_FRIENDLY_STRING.apply(infusionBuilder.build(livingEntity));
			final String icdString = String.format("%s/%s", tag.getTag(), type.getId());

			return sendFeedback(context, Text.translatable("commands.element.infuse.apply.success", elementString, icdString, entity.getName().getString()), true);
		} else {
			return sendError(context, Text.translatable("commands.element.infuse.failed.incompatible", entity.getName().getString()));
		}
	}

	private static int infuseDuration(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
		final Element element = ElementArgumentType.getElement(context, "element");
		final double gaugeUnits = DoubleArgumentType.getDouble(context, "gaugeUnits");
		final int duration = IntegerArgumentType.getInteger(context, "duration");
		final InternalCooldownTag tag = InternalCooldownTagType.getTagOrDefault(context, "tag", InternalCooldownTag.NONE);

		final @Nullable Reference<InternalCooldownType> typeRef = ClassInstanceUtil.cast(ElementCommand.getOrDefault(context, "type", Reference.class, null));
		final InternalCooldownType type = JavaScriptUtil.nullishCoalesing(
			ClassInstanceUtil.mapOrNull(typeRef, Reference::value),
			InternalCooldownType.DEFAULT
		);

		final Entity entity = context.getSource().getEntityOrThrow();

		if (!(entity instanceof final LivingEntity livingEntity))
			return sendError(context, Text.translatable("commands.enchant.failed.entity", entity.getName().getString()));

		final ItemStack stack = livingEntity.getMainHandStack();

		if (stack.isEmpty())
			return sendError(context, Text.translatable("commands.enchant.failed.itemless", entity.getName().getString()));

		if (!(stack.getItem() instanceof ToolItem))
			return sendError(context, Text.translatable("commands.element.infuse.failed.incompatible", entity.getName().getString()));

		final ElementalApplication.Builder infusionBuilder = ElementalApplications.builder()
			.setType(ElementalApplication.Type.DURATION)
			.setElement(element)
			.setGaugeUnits(gaugeUnits)
			.setDuration(duration);

		final InternalCooldownContext.Builder icdBuilder = InternalCooldownContext.builder()
			.setTag(tag)
			.setType(type);

		if (ElementalInfusionComponent.applyInfusion(stack, infusionBuilder, icdBuilder)) {
			final String elementString = ElementCommand.TO_FRIENDLY_STRING.apply(infusionBuilder.build(livingEntity));
			final String icdString = String.format("%s/%s", tag.getTag(), type.getId());

			return sendFeedback(context, Text.translatable("commands.element.infuse.apply.success", elementString, icdString, entity.getName().getString()), true);
		} else {
			return sendError(context, Text.translatable("commands.element.infuse.failed.incompatible", entity.getName().getString()));
		}
	}

	private static int infuseRemove(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
		final ServerCommandSource source = context.getSource();
		final Entity entity = source.getEntityOrThrow();

		if (!(entity instanceof final LivingEntity livingEntity))
			return sendError(context, Text.translatable("commands.enchant.failed.entity", entity.getName().getString()));

		final ItemStack stack = livingEntity.getMainHandStack();

		if (stack.isEmpty())
			return sendError(context, Text.translatable("commands.enchant.failed.itemless", entity.getName().getString()));

		if (!(stack.getItem() instanceof ToolItem))
			return sendError(context, Text.translatable("commands.element.infuse.failed.incompatible", entity.getName().getString()));

		return ElementalInfusionComponent.removeInfusion(stack)
			? sendFeedback(context, Text.translatable("commands.element.infuse.remove.success", entity.getName().getString()), true)
			: sendError(context, Text.translatable("commands.element.infuse.remove.none", entity.getName().getString()));
	}

	private static int sendError(CommandContext<ServerCommandSource> context, Text text) {
		context
			.getSource()
			.sendError(text);

		return 0;
	}

	private static int sendFeedback(CommandContext<ServerCommandSource> context, Text text, boolean broadcastToOps) {
		return sendFeedback(context, text, broadcastToOps, 1);
	}

	private static int sendFeedback(CommandContext<ServerCommandSource> context, Text text, boolean broadcastToOps, int value) {
		context
			.getSource()
			.sendFeedback(() -> text, broadcastToOps);

		return value;
	}

	private static <T> T getOrDefault(CommandContext<ServerCommandSource> context, String name, Class<T> clazz, T fallback) {
		try {
			return context.getArgument(name, clazz);
		} catch (IllegalArgumentException e) {
			return fallback;
		}
	}
}
