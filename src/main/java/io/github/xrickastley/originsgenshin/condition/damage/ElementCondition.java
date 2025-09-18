package io.github.xrickastley.originsgenshin.condition.damage;

import java.util.List;

import io.github.apace100.apoli.power.factory.condition.ConditionFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.xrickastley.originsgenshin.OriginsGenshin;
import io.github.xrickastley.originsgenshin.data.OriginsGenshinDataTypes;
import io.github.xrickastley.originsgenshin.element.Element;
import io.github.xrickastley.originsgenshin.element.ElementalDamageSource;

import net.minecraft.entity.damage.DamageSource;
import net.minecraft.util.Pair;

public class ElementCondition {
	private static boolean condition(SerializableData.Instance data, Pair<DamageSource, Float> pair) {
		final List<Element> elements = data.get("elements");

		return pair.getLeft() instanceof final ElementalDamageSource eds
			? elements.contains(eds.getElementalApplication().getElement())
			: elements.contains(Element.PHYSICAL); // Non-elemental Damage Sources are considered PHYSICAL.
	}

	public static ConditionFactory<Pair<DamageSource, Float>> getFactory() {
		return new ConditionFactory<>(
			OriginsGenshin.identifier("element"),
			new SerializableData()
				.add("elements", OriginsGenshinDataTypes.ELEMENTS),
			ElementCondition::condition
		);
	}
}
