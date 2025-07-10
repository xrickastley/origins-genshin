package io.github.xrickastley.originsgenshin.events;

import org.jetbrains.annotations.Nullable;

import io.github.xrickastley.originsgenshin.element.ElementalApplication;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;

@FunctionalInterface
public interface ElementRemoved {
	public static Event<ElementRemoved> EVENT = EventFactory.createArrayBacked(ElementRemoved.class,
		listeners -> application -> {
			for (final ElementRemoved listener : listeners) listener.onElementRemoved(application);
		}
	);

	void onElementRemoved(@Nullable ElementalApplication application);
}
