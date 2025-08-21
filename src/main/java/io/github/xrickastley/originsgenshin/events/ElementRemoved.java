package io.github.xrickastley.originsgenshin.events;

import io.github.xrickastley.originsgenshin.element.Element;
import io.github.xrickastley.originsgenshin.element.ElementalApplication;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;

@FunctionalInterface
public interface ElementRemoved {
	public static Event<ElementRemoved> EVENT = EventFactory.createArrayBacked(ElementRemoved.class,
		listeners -> (element, application) -> {
			for (final ElementRemoved listener : listeners) listener.onElementRemoved(element, application);
		}
	);

	void onElementRemoved(Element element, ElementalApplication application);
}
