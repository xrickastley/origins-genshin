package io.github.xrickastley.originsgenshin.events;

import org.jetbrains.annotations.Nullable;

import io.github.xrickastley.originsgenshin.element.ElementalApplication;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;

public interface ElementRefreshed {
	public static Event<ElementRefreshed> EVENT = EventFactory.createArrayBacked(ElementRefreshed.class,
		listeners -> (cur, prev) -> {
			for (final ElementRefreshed listener : listeners) listener.onElementRefreshed(cur, prev);
		}
	);

	void onElementRefreshed(@Nullable ElementalApplication current, @Nullable ElementalApplication previous);
}
