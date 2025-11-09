package io.github.xrickastley.originsgenshin.integration;

import java.util.function.Consumer;

import org.jetbrains.annotations.Nullable;

import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.power.Power;
import io.github.apace100.apoli.util.IdentifierAlias;
import io.github.xrickastley.originsgenshin.OriginsGenshin;
import io.github.xrickastley.originsgenshin.factory.OriginsGenshinFactories;
import io.github.xrickastley.originsgenshin.power.ActionOnElementAppliedPower;
import io.github.xrickastley.originsgenshin.power.ActionOnElementEventPower;
import io.github.xrickastley.originsgenshin.power.ActionOnElementRefreshedPower;
import io.github.xrickastley.originsgenshin.power.ActionOnElementRemovedPower;
import io.github.xrickastley.originsgenshin.power.ActionOnElementalReactionPower;
import io.github.xrickastley.sevenelements.element.Element;
import io.github.xrickastley.sevenelements.element.ElementalApplication;
import io.github.xrickastley.sevenelements.events.ElementEvents;
import io.github.xrickastley.sevenelements.events.ReactionTriggered;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.entity.LivingEntity;

public class SevenElementsIntegration implements ModIntegration {
	public void onIntegrationInitialize() {
		OriginsGenshinFactories.registerAll();

		IdentifierAlias.addNamespaceAlias("origins-genshin", "seven-elements");

		ElementEvents.APPLIED
			.register((element, application) -> callElementEventActions(ActionOnElementAppliedPower.class, element, application));

		ElementEvents.REFRESHED
			.register((element, oldApp, newApp) -> callElementEventActions(ActionOnElementRefreshedPower
			.class, element, newApp));

		ElementEvents.REMOVED
			.register((element, application) -> callElementEventActions(ActionOnElementRemovedPower.class, element, application));

		ReactionTriggered.EVENT
			.register((reaction, reducedGauge, target, origin) ->
				callEventActions(ActionOnElementalReactionPower.class, target, power -> power.trigger(reaction, target, origin))
			);
	}

	private <T extends ActionOnElementEventPower> void callElementEventActions(Class<T> eventClass, Element element, @Nullable ElementalApplication application) {
		if (application == null) return;

		final LivingEntity entity = application.getEntity();

		this.callEventActions(eventClass, entity, power -> power.trigger(element, entity));
	}

	private <T extends Power> void callEventActions(Class<T> eventClass, LivingEntity entity, Consumer<T> powerConsumer) {
		PowerHolderComponent
			.getPowers(entity, eventClass)
			.forEach(powerConsumer);
	}

	public static void requireSevenElements(Runnable runnable) {
		if (!SevenElementsIntegration.hasSevenElements()) return;

		try {
			runnable.run();
		} catch (Exception e) {
			OriginsGenshin.LOGGER.error("An unexpected exception occured while trying to run Seven Elements-only code!", e);
		}
	}

	public static boolean hasSevenElements() {
		final FabricLoader loader = FabricLoader.getInstance();

		return loader.isModLoaded("seven-elements");
	}
}
