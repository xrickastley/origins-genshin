package io.github.xrickastley.originsgenshin.events;

import org.jetbrains.annotations.Nullable;

import io.github.xrickastley.originsgenshin.element.Element;
import io.github.xrickastley.originsgenshin.element.ElementalApplication;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;

public final class ElementEvents {
	public static final Event<ElementApplied> APPLIED = EventFactory.createArrayBacked(ElementApplied.class,
		listeners -> (element, application) -> {
			for (final ElementApplied listener : listeners) listener.onElementApplied(element, application);
		}
	);

	public static final Event<ElementRefreshed> REFRESHED = EventFactory.createArrayBacked(ElementRefreshed.class,
		listeners -> (element, cur, prev) -> {
			for (final ElementRefreshed listener : listeners) listener.onElementRefreshed(element, cur, prev);
		}
	);

	public static final Event<ElementRemoved> REMOVED = EventFactory.createArrayBacked(ElementRemoved.class,
		listeners -> (element, application) -> {
			for (final ElementRemoved listener : listeners) listener.onElementRemoved(element, application);
		}
	);

	@FunctionalInterface
	public interface ElementApplied {
		void onElementApplied(Element element, @Nullable ElementalApplication application);
	}

	@FunctionalInterface
	public interface ElementRefreshed {
		void onElementRefreshed(Element element, @Nullable ElementalApplication current, @Nullable ElementalApplication previous);
	}

	@FunctionalInterface
	public interface ElementRemoved {
		void onElementRemoved(Element element, ElementalApplication application);
	}
}
