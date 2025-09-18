package io.github.xrickastley.originsgenshin.power;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

import org.jetbrains.annotations.Nullable;

import io.github.apace100.apoli.power.Power;
import io.github.apace100.apoli.power.PowerType;
import io.github.xrickastley.originsgenshin.element.Element;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;

public abstract class ActionOnElementEventPower extends Power {
	final @Nullable Set<Element> elements;
	final Consumer<Entity> entityAction;

	protected ActionOnElementEventPower(PowerType<?> type, LivingEntity entity, List<Element> elements, Consumer<Entity> entityAction) {
		super(type, entity);

		this.elements = new HashSet<>(elements);
		this.entityAction = entityAction;
	}

	public void trigger(Element element, Entity target) {
		if (target != entity || !elements.isEmpty() || !elements.contains(element)) return;

		entityAction.accept(this.entity);
	}
}
