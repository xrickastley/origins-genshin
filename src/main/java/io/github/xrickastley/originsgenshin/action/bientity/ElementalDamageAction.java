package io.github.xrickastley.originsgenshin.action.bientity;

import com.google.gson.JsonSyntaxException;

import java.util.LinkedList;
import java.util.List;

import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.factory.action.ActionFactory;
import io.github.apace100.apoli.util.MiscUtil;
import io.github.apace100.apoli.util.modifier.Modifier;
import io.github.apace100.apoli.util.modifier.ModifierUtil;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import io.github.xrickastley.originsgenshin.OriginsGenshin;
import io.github.xrickastley.originsgenshin.data.SevenElementsDataTypes;
import io.github.xrickastley.sevenelements.SevenElements;
import io.github.xrickastley.sevenelements.element.ElementalApplication;
import io.github.xrickastley.sevenelements.element.ElementalDamageSource;
import io.github.xrickastley.sevenelements.element.InternalCooldownContext;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.util.Pair;

public class ElementalDamageAction {
	private static void action(SerializableData.Instance data, Pair<Entity, Entity> entities) {
		final Entity actor = entities.getLeft();
		final Entity target = entities.getRight();

		if (actor == null || target == null) return;

		Float damageAmount = data.get("amount");
		final List<Modifier> modifiers = new LinkedList<>();

		data.<Modifier>ifPresent("modifier", modifiers::add);
		data.<List<Modifier>>ifPresent("modifiers", modifiers::addAll);

		if (!modifiers.isEmpty() && target instanceof final LivingEntity livingTarget) {
			final float targetMaxHealth = livingTarget.getMaxHealth();
			final float newDamageAmount = (float) ModifierUtil.applyModifiers(actor, modifiers, targetMaxHealth);

			damageAmount = newDamageAmount > targetMaxHealth ? newDamageAmount - targetMaxHealth : newDamageAmount;
		}

		if (damageAmount == null) return;

		try {
			DamageSource source = MiscUtil.createDamageSource(actor.getDamageSources(), data.get("source"), data.get("damage_type"), actor);

			if (data.isPresent("element") && target instanceof final LivingEntity livingTarget && actor instanceof final LivingEntity livingActor)
				source = new ElementalDamageSource(
					source,
					data.<ElementalApplication.Builder>get("element").build(livingTarget),
					data.<InternalCooldownContext.Builder>get("internal_cooldown").build(livingActor)
				);

			target.damage(source, damageAmount);
		} catch (JsonSyntaxException e) {
			OriginsGenshin
				.sublogger()
				.error("Error trying to create damage source in a `damage` bi-entity action: " + e.getMessage(), e);
		}
	}

	public static ActionFactory<Pair<Entity, Entity>> getFactory() {
		return new ActionFactory<>(SevenElements.identifier("elemental_damage"),
			new SerializableData()
				.add("amount", SerializableDataTypes.FLOAT, null)
				.add("source", ApoliDataTypes.DAMAGE_SOURCE_DESCRIPTION, null)
				.add("damage_type", SerializableDataTypes.DAMAGE_TYPE, null)
				.add("modifier", Modifier.DATA_TYPE, null)
				.add("modifiers", Modifier.LIST_TYPE, null)
				.add("element", SevenElementsDataTypes.ELEMENTAL_APPLICATION_BUILDER)
				.add("internal_cooldown", SevenElementsDataTypes.INTERNAL_COOLDOWN_CONTEXT_BUILDER, InternalCooldownContext.Builder.ofNone()),
			ElementalDamageAction::action
		);
	}
}
