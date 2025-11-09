package io.github.xrickastley.originsgenshin.action.entity;

import io.github.apace100.apoli.power.factory.action.ActionFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.xrickastley.originsgenshin.data.SevenElementsDataTypes;
import io.github.xrickastley.sevenelements.SevenElements;
import io.github.xrickastley.sevenelements.component.ElementComponent;
import io.github.xrickastley.sevenelements.element.ElementalApplication;
import io.github.xrickastley.sevenelements.element.InternalCooldownContext;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;

public class ApplyElementAction {
	private static void action(SerializableData.Instance data, Entity entity) {
		final ElementalApplication.Builder applicationBuilder = data.get("element");
		final InternalCooldownContext.Builder icdBuilder = data.get("internal_cooldown");

		final ElementComponent component = ElementComponent.KEY.get(entity);

		if (component == null || !(entity instanceof final LivingEntity livingEntity)) return;

		component.addElementalApplication(
			applicationBuilder.build(livingEntity),
			icdBuilder.build(livingEntity)
		);
	}

	public static ActionFactory<Entity> getFactory() {
		return new ActionFactory<>(SevenElements.identifier("apply_element"),
			new SerializableData()
				.add("element", SevenElementsDataTypes.ELEMENTAL_APPLICATION_BUILDER)
				.add("internal_cooldown", SevenElementsDataTypes.INTERNAL_COOLDOWN_CONTEXT_BUILDER, InternalCooldownContext.Builder.ofNone()),
			ApplyElementAction::action
		);
	}

}
