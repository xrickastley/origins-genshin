package io.github.xrickastley.originsgenshin.condition.entity;

import java.util.List;

import io.github.apace100.apoli.power.factory.condition.ConditionFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.xrickastley.originsgenshin.data.SevenElementsDataTypes;
import io.github.xrickastley.sevenelements.SevenElements;
import io.github.xrickastley.sevenelements.component.ElementComponent;
import io.github.xrickastley.sevenelements.element.Element;

import net.minecraft.entity.Entity;

public class HasElementCondition {
	private static boolean condition(SerializableData.Instance data, Entity entity) {
		final List<Element> elements = data.get("elements");

		final ElementComponent component = ElementComponent.KEY.get(entity);

		if (component == null) return false;

		return elements
			.stream()
			.anyMatch(component::hasElementalApplication);
	}

	public static ConditionFactory<Entity> getFactory() {
		return new ConditionFactory<>(
			SevenElements.identifier("has_element"),
			new SerializableData()
				.add("elements", SevenElementsDataTypes.ELEMENTS),
			HasElementCondition::condition
		);
	}
}
