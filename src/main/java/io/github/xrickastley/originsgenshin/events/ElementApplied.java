package io.github.xrickastley.originsgenshin.events;

import org.jetbrains.annotations.Nullable;

import io.github.xrickastley.originsgenshin.element.ElementalApplication;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;

@FunctionalInterface
public interface ElementApplied {
	public static Event<ElementApplied> EVENT = EventFactory.createArrayBacked(ElementApplied.class,
		listeners -> application -> {
			for (final ElementApplied listener : listeners) listener.onElementApplied(application);
		}
	);

	void onElementApplied(@Nullable ElementalApplication application);
}
