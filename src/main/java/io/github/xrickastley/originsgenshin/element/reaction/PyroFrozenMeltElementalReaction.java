package io.github.xrickastley.originsgenshin.element.reaction;

import io.github.xrickastley.originsgenshin.element.Element;
import io.github.xrickastley.originsgenshin.element.ElementalApplication;
import io.github.xrickastley.originsgenshin.factory.OriginsGenshinStatusEffects;

import net.minecraft.entity.LivingEntity;

import javax.annotation.Nullable;

public final class PyroFrozenMeltElementalReaction extends AbstractPyroMeltElementalReaction {
	PyroFrozenMeltElementalReaction() {
		super("Melt", "melt_pyro-frozen", Element.FREEZE);
	}

	@Override
	protected void onReaction(LivingEntity entity, ElementalApplication auraElement, ElementalApplication triggeringElement, double reducedGauge, @Nullable LivingEntity origin) {
		super.onReaction(entity, auraElement, triggeringElement, reducedGauge, origin);

		// Remove frozen effect upon Melt.
		entity.removeStatusEffect(OriginsGenshinStatusEffects.FROZEN);
	}
}
