package io.github.xrickastley.originsgenshin.action.entity;

import io.github.apace100.apoli.power.factory.action.ActionFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.xrickastley.originsgenshin.OriginsGenshin;
import io.github.xrickastley.originsgenshin.component.ElementComponent;
import io.github.xrickastley.originsgenshin.data.OriginsGenshinDataTypes;
import io.github.xrickastley.originsgenshin.element.ElementalApplication;
import io.github.xrickastley.originsgenshin.element.InternalCooldownContext;

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
		return new ActionFactory<>(OriginsGenshin.identifier("apply_element"),
			new SerializableData()
				.add("element", OriginsGenshinDataTypes.ELEMENTAL_APPLICATION_BUILDER)
				.add("internal_cooldown", OriginsGenshinDataTypes.INTERNAL_COOLDOWN_CONTEXT_BUILDER, InternalCooldownContext.Builder.ofNone()),
			ApplyElementAction::action
		);
	}

}
