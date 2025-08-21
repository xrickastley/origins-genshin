package io.github.xrickastley.originsgenshin.condition.entity;

import java.util.List;

import io.github.apace100.apoli.power.factory.condition.ConditionFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.xrickastley.originsgenshin.OriginsGenshin;
import io.github.xrickastley.originsgenshin.component.ElementComponent;
import io.github.xrickastley.originsgenshin.data.OriginsGenshinDataTypes;
import io.github.xrickastley.originsgenshin.element.Element;
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
			OriginsGenshin.identifier("has_element"),
			new SerializableData()
				.add("elements", OriginsGenshinDataTypes.ELEMENTS),
			HasElementCondition::condition
		);
	}
}
