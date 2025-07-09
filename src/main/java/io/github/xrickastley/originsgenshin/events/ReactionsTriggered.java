package io.github.xrickastley.originsgenshin.events;

import blue.endless.jankson.annotation.Nullable;
import io.github.xrickastley.originsgenshin.element.reaction.ElementalReaction;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.entity.LivingEntity;

@FunctionalInterface
public interface ReactionsTriggered {
	public static Event<ReactionsTriggered> EVENT = EventFactory.createArrayBacked(ReactionsTriggered.class,
		listeners -> (reaction, target, origin) -> {
			for (final ReactionsTriggered listener : listeners) listener.onReactionsTriggered(reaction, target, origin);
		}
	);

	void onReactionsTriggered(ElementalReaction reaction, LivingEntity target, @Nullable LivingEntity origin);
}
