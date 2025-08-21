package io.github.xrickastley.originsgenshin.events;

import org.jetbrains.annotations.Nullable;

import io.github.xrickastley.originsgenshin.element.Element;
import io.github.xrickastley.originsgenshin.element.ElementalApplication;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;

public interface ElementRefreshed {
	public static Event<ElementRefreshed> EVENT = EventFactory.createArrayBacked(ElementRefreshed.class,
		listeners -> (element, cur, prev) -> {
			for (final ElementRefreshed listener : listeners) listener.onElementRefreshed(element, cur, prev);
		}
	);

	void onElementRefreshed(Element element, @Nullable ElementalApplication current, @Nullable ElementalApplication previous);
}
