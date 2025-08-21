package io.github.xrickastley.originsgenshin.action.entity;

import com.google.gson.JsonSyntaxException;

import java.util.LinkedList;
import java.util.List;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.factory.action.ActionFactory;
import io.github.apace100.apoli.util.MiscUtil;
import io.github.apace100.apoli.util.modifier.Modifier;
import io.github.apace100.apoli.util.modifier.ModifierUtil;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import io.github.xrickastley.originsgenshin.OriginsGenshin;
import io.github.xrickastley.originsgenshin.data.OriginsGenshinDataTypes;
import io.github.xrickastley.originsgenshin.element.ElementalApplication;
import io.github.xrickastley.originsgenshin.element.ElementalDamageSource;
import io.github.xrickastley.originsgenshin.element.InternalCooldownContext;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;

public class ElementalDamageAction {
	private static void action(SerializableData.Instance data, Entity entity) {
		Float damageAmount = data.get("amount");
		final List<Modifier> modifiers = new LinkedList<>();

		data.<Modifier>ifPresent("modifier", modifiers::add);
		data.<List<Modifier>>ifPresent("modifiers", modifiers::addAll);

		if (!modifiers.isEmpty() && entity instanceof final LivingEntity livingEntity) {
			final float maxHealth = livingEntity.getMaxHealth();
			final float newDamageAmount = (float) ModifierUtil.applyModifiers(livingEntity, modifiers, maxHealth);

			damageAmount = newDamageAmount > maxHealth ? newDamageAmount - maxHealth : newDamageAmount;
		}

		if (damageAmount == null) return;

		try {
			DamageSource source = MiscUtil.createDamageSource(entity.getDamageSources(), data.get("source"), data.get("damage_type"));

			if (data.isPresent("element") && entity instanceof final LivingEntity livingEntity)
				source = new ElementalDamageSource(
					source,
					data.<ElementalApplication.Builder>get("element").build(livingEntity),
					data.<InternalCooldownContext.Builder>get("internal_cooldown").build(livingEntity)
				);

			entity.damage(source, damageAmount);
		} catch (JsonSyntaxException e) {
			Apoli.LOGGER.error("Error trying to create damage source in a `damage` entity action: " + e.getMessage());
		}
	}

	public static ActionFactory<Entity> getFactory() {
		return new ActionFactory<>(OriginsGenshin.identifier("elemental_damage"),
			new SerializableData()
				.add("amount", SerializableDataTypes.FLOAT, null)
				.add("source", ApoliDataTypes.DAMAGE_SOURCE_DESCRIPTION, null)
				.add("damage_type", SerializableDataTypes.DAMAGE_TYPE, null)
				.add("modifier", Modifier.DATA_TYPE, null)
				.add("modifiers", Modifier.LIST_TYPE, null)
				.add("element", OriginsGenshinDataTypes.ELEMENTAL_APPLICATION_BUILDER)
				.add("internal_cooldown", OriginsGenshinDataTypes.INTERNAL_COOLDOWN_CONTEXT_BUILDER, InternalCooldownContext.Builder.ofNone()),
			ElementalDamageAction::action
		);
	}
}
