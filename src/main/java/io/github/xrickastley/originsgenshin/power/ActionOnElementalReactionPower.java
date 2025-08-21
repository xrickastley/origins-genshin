package io.github.xrickastley.originsgenshin.power;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

import org.jetbrains.annotations.Nullable;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.Power;
import io.github.apace100.apoli.power.PowerType;
import io.github.apace100.apoli.power.factory.PowerFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.xrickastley.originsgenshin.data.OriginsGenshinDataTypes;
import io.github.xrickastley.originsgenshin.element.reaction.ElementalReaction;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Pair;

public class ActionOnElementalReactionPower extends Power {
	private final Set<ElementalReaction> reactions;
	private final @Nullable Consumer<Entity> entityAction;
	private final @Nullable Consumer<Pair<Entity, Entity>> bientityAction;

	protected ActionOnElementalReactionPower(PowerType<?> type, LivingEntity entity, List<ElementalReaction> reactions, Consumer<Entity> entityAction, Consumer<Pair<Entity, Entity>> bientityAction) {
		super(type, entity);

		this.reactions = new HashSet<>(reactions);
		this.entityAction = entityAction;
		this.bientityAction = bientityAction;
	}

	public void trigger(ElementalReaction reaction, Entity target, @Nullable Entity origin) {
		if (target != entity || !reactions.isEmpty() || !reactions.contains(reaction)) return;

		if (origin == null && entityAction != null) {
			entityAction.accept(target);
		} else if (bientityAction != null) {
			bientityAction.accept(new Pair<>(target, origin));
		}
	}

    public static PowerFactory<?> createFactory() {
        return new PowerFactory<>(
            Apoli.identifier("action_on_elemental_reaction"),
            new SerializableData()
				.add("reactions", OriginsGenshinDataTypes.ELEMENTAL_REACTIONS)
                .add("entity_action", ApoliDataTypes.ENTITY_ACTION, null)
                .add("bientity_action", ApoliDataTypes.ENTITY_ACTION, null),
            data -> (powerType, livingEntity) -> new ActionOnElementalReactionPower(
                powerType,
                livingEntity,
                data.get("reactions"),
                data.get("entity_action"),
                data.get("bientity_action")
            )
        ).allowCondition();
    }
}
