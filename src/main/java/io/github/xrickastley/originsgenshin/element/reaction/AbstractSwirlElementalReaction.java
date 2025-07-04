package io.github.xrickastley.originsgenshin.element.reaction;

import javax.annotation.Nullable;

import io.github.xrickastley.originsgenshin.OriginsGenshin;
import io.github.xrickastley.originsgenshin.element.Element;
import io.github.xrickastley.originsgenshin.element.ElementalApplication;
import io.github.xrickastley.originsgenshin.element.ElementalDamageSource;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageTypes;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;

public abstract class AbstractSwirlElementalReaction extends ElementalReaction {
	private static final double radius = 3;
	private final Element swirlElement;

	/**
	 * Creates a Swirl reaction with the specified settings. <br> <br>
	 * 
	 * The specified <b>aura element</b> will serve as the "swirlable" element. <br> <br> 
	 * 
	 * For example, if the Aura Element is {@link Element#PYRO}, then the Pyro element is swirled
	 * and spread to nearby targets (r=3m). <br> <br>
	 * 
	 * For the Gauge Units applied by the Swirl reaction, as well as it's duration, you may refer
	 * here: <a href=https://genshin-impact.fandom.com/wiki/Elemental_Gauge_Theory/Advanced_Mechanics#Swirl_Elemental_Application">
	 * Swirl Elemental Application</a> 
	 * @param settings The {@code ElementalReactionSettings} for this {@code ElementalReaction}.
	 */
	protected AbstractSwirlElementalReaction(ElementalReactionSettings settings) {
		super(settings);

		this.swirlElement = settings.auraElement.getLeft();
	}

	@Override
	protected void onReaction(LivingEntity entity, ElementalApplication auraElement, ElementalApplication triggeringElement, double reducedGauge, @Nullable LivingEntity origin) {
		final World world = entity.getWorld();

		final double gaugeOriginAura = auraElement.getCurrentGauge() + reducedGauge;
		final double gaugeAnemo = triggeringElement.getCurrentGauge() + reducedGauge;

		final double gaugeReaction = gaugeOriginAura >= (0.5 * gaugeAnemo)
			? gaugeAnemo
			: gaugeOriginAura;

		final double gaugeSwirlAttack = ((gaugeReaction - 0.04) * 1.25) + 1;

		for (final LivingEntity target : world.getNonSpectatingEntities(LivingEntity.class, Box.of(entity.getLerpedPos(1F), radius * 2, radius * 2, radius * 2))) {
			if (target == origin) continue;

			final ElementalDamageSource source = new ElementalDamageSource(
				world
					.getDamageSources()
					.create(DamageTypes.PLAYER_ATTACK, origin),
				ElementalApplication.gaugeUnits(target, swirlElement, gaugeSwirlAttack, true),
				String.format("swirl-%s", swirlElement.toString().toLowerCase())
			);
			final float damage = 2 * OriginsGenshin.getLevelMultiplier(world);

			target.damage(source, damage);
		}
	}
}