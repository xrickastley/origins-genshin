package io.github.xrickastley.originsgenshin.power;

import java.util.List;
import java.util.function.Consumer;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.PowerType;
import io.github.apace100.apoli.power.factory.PowerFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.xrickastley.originsgenshin.data.OriginsGenshinDataTypes;
import io.github.xrickastley.originsgenshin.element.Element;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;

public final class ActionOnElementAppliedPower extends ActionOnElementEventPower {
	private ActionOnElementAppliedPower(PowerType<?> type, LivingEntity entity, List<Element> elements, Consumer<Entity> entityAction) {
		super(type, entity, elements, entityAction);
	}

    public static PowerFactory<?> createFactory() {
        return new PowerFactory<>(
            Apoli.identifier("action_on_element_applied"),
            new SerializableData()
				.add("elements", OriginsGenshinDataTypes.ELEMENTS)
                .add("entity_action", ApoliDataTypes.ENTITY_ACTION),
            data -> (powerType, livingEntity) -> new ActionOnElementAppliedPower(
                powerType,
                livingEntity,
                data.get("elements"),
                data.get("entity_action")
            )
        ).allowCondition();
    }
}
