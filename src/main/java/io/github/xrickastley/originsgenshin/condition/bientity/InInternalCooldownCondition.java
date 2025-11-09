package io.github.xrickastley.originsgenshin.condition.bientity;

import org.jetbrains.annotations.Nullable;

import io.github.apace100.apoli.power.factory.condition.ConditionFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.xrickastley.originsgenshin.data.SevenElementsDataTypes;
import io.github.xrickastley.sevenelements.SevenElements;
import io.github.xrickastley.sevenelements.component.ElementComponent;
import io.github.xrickastley.sevenelements.element.Element;
import io.github.xrickastley.sevenelements.element.InternalCooldownContext;
import io.github.xrickastley.sevenelements.util.ClassInstanceUtil;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Pair;

public class InInternalCooldownCondition {
	private static boolean condition(SerializableData.Instance data, Pair<Entity, Entity> pair) {
		final @Nullable LivingEntity actor = ClassInstanceUtil.castOrNull(pair.getLeft(), LivingEntity.class);
		final @Nullable LivingEntity target = ClassInstanceUtil.castOrNull(pair.getRight(), LivingEntity.class);

		if (actor == null || target == null) return false;

		final Element element = data.get("element");
		final InternalCooldownContext icdContext = data.<InternalCooldownContext.Builder>get("internal_cooldown").build(actor);
		final ElementComponent targetComponent = ElementComponent.KEY.get(target);

		if (targetComponent == null) return false;

		return icdContext
			.getInternalCooldown(targetComponent.getElementHolder(element))
			.isInInternalCooldown();
	}

	public static ConditionFactory<Pair<Entity, Entity>> getFactory() {
		return new ConditionFactory<>(
			SevenElements.identifier("in_internal_cooldown"),
			new SerializableData()
				.add("element", SevenElementsDataTypes.ELEMENT)
				.add("internal_cooldown", SevenElementsDataTypes.INTERNAL_COOLDOWN_CONTEXT_BUILDER, InternalCooldownContext.Builder.ofNone()),
			InInternalCooldownCondition::condition
		);
	}
}
