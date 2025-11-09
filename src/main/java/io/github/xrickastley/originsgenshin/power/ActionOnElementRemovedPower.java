package io.github.xrickastley.originsgenshin.power;

import java.util.List;
import java.util.function.Consumer;

import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.PowerType;
import io.github.apace100.apoli.power.factory.PowerFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.xrickastley.originsgenshin.data.SevenElementsDataTypes;
import io.github.xrickastley.sevenelements.SevenElements;
import io.github.xrickastley.sevenelements.element.Element;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;

public final class ActionOnElementRemovedPower extends ActionOnElementEventPower {
	private ActionOnElementRemovedPower(PowerType<?> type, LivingEntity entity, List<Element> elements, Consumer<Entity> entityAction) {
		super(type, entity, elements, entityAction);
	}

    public static PowerFactory<?> createFactory() {
        return new PowerFactory<>(
            SevenElements.identifier("action_on_element_removed"),
            new SerializableData()
				.add("elements", SevenElementsDataTypes.ELEMENTS)
                .add("entity_action", ApoliDataTypes.ENTITY_ACTION),
            data -> (powerType, livingEntity) -> new ActionOnElementRemovedPower(
                powerType,
                livingEntity,
                data.get("elements"),
                data.get("entity_action")
            )
        ).allowCondition();
    }
}
