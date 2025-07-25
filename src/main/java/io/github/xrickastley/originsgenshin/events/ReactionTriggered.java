package io.github.xrickastley.originsgenshin.events;

import io.github.xrickastley.originsgenshin.element.reaction.ElementalReaction;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.entity.LivingEntity;

import blue.endless.jankson.annotation.Nullable;

@FunctionalInterface
public interface ReactionTriggered {
	public static Event<ReactionTriggered> EVENT = EventFactory.createArrayBacked(ReactionTriggered.class,
		listeners -> (reaction, reducedGauge, target, origin) -> {
			for (final ReactionTriggered listener : listeners) listener.onReactionTriggered(reaction, reducedGauge, target, origin);
		}
	);

	void onReactionTriggered(ElementalReaction reaction, double reducedGauge, LivingEntity target, @Nullable LivingEntity origin);
}
