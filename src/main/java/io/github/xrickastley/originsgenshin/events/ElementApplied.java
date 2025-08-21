package io.github.xrickastley.originsgenshin.events;

import org.jetbrains.annotations.Nullable;

import io.github.xrickastley.originsgenshin.element.Element;
import io.github.xrickastley.originsgenshin.element.ElementalApplication;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;

@FunctionalInterface
public interface ElementApplied {
	public static Event<ElementApplied> EVENT = EventFactory.createArrayBacked(ElementApplied.class,
		listeners -> (element, application) -> {
			for (final ElementApplied listener : listeners) listener.onElementApplied(element, application);
		}
	);

	void onElementApplied(Element element, @Nullable ElementalApplication application);
}
