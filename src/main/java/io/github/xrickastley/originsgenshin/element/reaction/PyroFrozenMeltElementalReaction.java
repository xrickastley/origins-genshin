package io.github.xrickastley.originsgenshin.element.reaction;

import javax.annotation.Nullable;

import io.github.xrickastley.originsgenshin.element.Element;
import io.github.xrickastley.originsgenshin.element.ElementalApplication;
import io.github.xrickastley.originsgenshin.factory.OriginsGenshinStatusEffects;
import net.minecraft.entity.LivingEntity;

public final class PyroFrozenMeltElementalReaction extends AbstractPyroMeltElementalReaction {
	protected PyroFrozenMeltElementalReaction() {
		super("Melt (Pyro)", "melt_pyro-frozen", Element.FROZEN, 3);
	}

	@Override
	protected void onReaction(LivingEntity entity, ElementalApplication auraElement, ElementalApplication triggeringElement, double reducedGauge, @Nullable LivingEntity origin) {
		super.onReaction(entity, auraElement, triggeringElement, reducedGauge, origin);

		// Remove frozen effect upon Melt.
		entity.removeStatusEffect(OriginsGenshinStatusEffects.FROZEN);
	}
}
