package io.github.xrickastley.originsgenshin.events;

import io.github.xrickastley.originsgenshin.element.reaction.ElementalReaction;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.entity.LivingEntity;

public interface ReactionTriggered {
	public static Event<ReactionTriggered> EVENT = EventFactory.createArrayBacked(ReactionTriggered.class,
		listeners -> (reaction, entity) -> {
			for (final ReactionTriggered listener : listeners) listener.onReactionTriggered(reaction, entity);
		}
	);

	void onReactionTriggered(ElementalReaction reaction, LivingEntity entity);
}
