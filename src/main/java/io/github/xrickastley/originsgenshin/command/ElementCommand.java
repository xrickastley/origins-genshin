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
import io.github.xrickastley.originsgenshin.element.Element;
import io.github.xrickastley.originsgenshin.element.ElementHolder;
import io.github.xrickastley.originsgenshin.element.ElementalApplication;
import io.github.xrickastley.originsgenshin.element.ElementalApplications;
import io.github.xrickastley.originsgenshin.element.InternalCooldownContext;
import io.github.xrickastley.originsgenshin.element.reaction.ElementalReaction;
import io.github.xrickastley.originsgenshin.util.Array;
import io.github.xrickastley.originsgenshin.util.Functions;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.text.Texts;
import net.minecraft.util.Identifier;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class ElementCommand {
	private static final Function<ElementalApplication, String> TO_FRIENDLY_STRING = a -> {
		final String element = a.getElement().toString().substring(0, 1).toUpperCase() + a.getElement().toString().substring(1).toLowerCase();

		return a.isDuration()
			? String.format("%.3fU %s (%.2fs left)", a.getCurrentGauge(), element, a.getRemainingTicks() / 20.0)
			: String.format("%.3fU %s", a.getCurrentGauge(), element);
	};

	public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
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
		);
	}

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

		if (!holder.hasElementalApplication()) {
			context
				.getSource()
				.sendError(Text.translatable("commands.element.failed.none", entity.getDisplayName(), element));

			return 0;
		}

		final double reducedGauge = holder
			.getElementalApplication()
			.reduceGauge(gaugeUnits);

		ElementComponent.sync(entity);

		context
			.getSource()
			.sendFeedback(
				() -> Text.translatable("commands.element.reduce", element, reducedGauge),
				true
			);

		return 1;
	}

	private static int queryElements(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
		final Entity entity = EntityArgumentType.getEntity(context, "target");

		if (!(entity instanceof final LivingEntity target)) {
			context
				.getSource()
				.sendError(Text.translatable("commands.element.failed.entity", entity));

			return 0;
		}

		final ElementComponent component = ElementComponent.KEY.get(target);
		final Array<ElementalApplication> appliedElements = component.getAppliedElements();

		if (appliedElements.isEmpty()) {
			context
				.getSource()
				.sendError(Text.translatable("commands.element.query.multiple.none", entity));

			return 0;
		}

		context
			.getSource()
			.sendFeedback(
				() -> Text.translatable("commands.element.query.multiple.success", entity.getDisplayName(), Texts.join(appliedElements, Functions.compose(ElementCommand.TO_FRIENDLY_STRING, Text::literal))), 
				true
			);

		return 1;
	}

	private static int queryElement(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
		final Entity entity = EntityArgumentType.getEntity(context, "target");
		final Element element = ElementArgumentType.getElement(context, "element");

		if (!(entity instanceof final LivingEntity target)) {
			context
				.getSource()
				.sendError(Text.translatable("commands.element.failed.entity", entity));

			return 0;
		}

		final ElementComponent component = ElementComponent.KEY.get(target);
		final @Nullable ElementalApplication application = component.getElementHolder(element).getElementalApplication();

		if (application == null) {
			context
				.getSource()
				.sendError(Text.translatable("commands.element.query.single.none", entity.getDisplayName(), element));

			return 0;
		}

		context
			.getSource()
			.sendFeedback(
				() -> Text.translatable("commands.element.query.single.success", entity.getDisplayName(), ElementCommand.TO_FRIENDLY_STRING.apply(application)), 
				true
			);

		return application != null ? 1 : 0;
	}

	private static <T> T getOrDefault(CommandContext<ServerCommandSource> context, String name, Class<T> clazz, T fallback) {
		try {
			return context.getArgument(name, clazz);
		} catch (IllegalArgumentException e) {
			return fallback;
		}
	}
}
