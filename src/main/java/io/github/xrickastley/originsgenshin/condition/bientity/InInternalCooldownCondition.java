package io.github.xrickastley.originsgenshin.condition.bientity;

import org.jetbrains.annotations.Nullable;

import io.github.apace100.apoli.power.factory.condition.ConditionFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.xrickastley.originsgenshin.OriginsGenshin;
import io.github.xrickastley.originsgenshin.component.ElementComponent;
import io.github.xrickastley.originsgenshin.data.OriginsGenshinDataTypes;
import io.github.xrickastley.originsgenshin.element.Element;
import io.github.xrickastley.originsgenshin.element.InternalCooldownContext;
import io.github.xrickastley.originsgenshin.util.ClassInstanceUtil;

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
			OriginsGenshin.identifier("in_internal_cooldown"),
			new SerializableData()
				.add("element", OriginsGenshinDataTypes.ELEMENT)
				.add("internal_cooldown", OriginsGenshinDataTypes.INTERNAL_COOLDOWN_CONTEXT_BUILDER, InternalCooldownContext.Builder.ofNone()),
			InInternalCooldownCondition::condition
		);
	}
}
