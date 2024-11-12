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
	 * For example, if the Aura Element is {@code PYRO}, then the Pyro element is swirled
	 * and spread to nearby targets (r=3m). <br> <br>
	 * 
	 * Due to a lack of documentation in the wiki, the applied element will have a gauge unit
	 * value equal to the minimum of either the Anemo or Swirled element.
	 * @param settings
	 */
	protected AbstractSwirlElementalReaction(ElementalReactionSettings settings) {
		super(settings);

		this.swirlElement = settings.auraElement.getLeft();
	}

	@Override
	protected void onReaction(LivingEntity entity, ElementalApplication auraElement, ElementalApplication triggeringElement, double reducedGauge, @Nullable LivingEntity origin) {
		final World world = entity.getWorld();

		for (final LivingEntity target : world.getNonSpectatingEntities(LivingEntity.class, Box.of(entity.getLerpedPos(1F), radius * 2, radius * 2, radius * 2))) {
			if (target == origin) continue;

			final ElementalDamageSource source = new ElementalDamageSource(
				world
					.getDamageSources()
					.create(DamageTypes.PLAYER_ATTACK, origin),
				ElementalApplication.usingGaugeUnits(target, swirlElement, Math.min(auraElement.getGaugeUnits(), triggeringElement.getGaugeUnits())),
				String.format("swirl-%s", swirlElement.toString().toLowerCase())
			);
			final float damage = 2 * OriginsGenshin.getLevelMultiplier(world);

			target.damage(source, damage);
		}
	}
}