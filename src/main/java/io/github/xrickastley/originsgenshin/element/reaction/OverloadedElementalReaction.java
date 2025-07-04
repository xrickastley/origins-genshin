package io.github.xrickastley.originsgenshin.element.reaction;

import javax.annotation.Nullable;

import io.github.xrickastley.originsgenshin.OriginsGenshin;
import io.github.xrickastley.originsgenshin.component.ElementComponent;
import io.github.xrickastley.originsgenshin.element.Element;
import io.github.xrickastley.originsgenshin.element.ElementalApplication;
import io.github.xrickastley.originsgenshin.element.ElementalDamageSource;
import io.github.xrickastley.originsgenshin.factory.OriginsGenshinParticleFactory;
import io.github.xrickastley.originsgenshin.util.NonEntityDamagingExplosion;

import net.minecraft.entity.LivingEntity;
import net.minecraft.network.packet.s2c.play.ExplosionS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;
import net.minecraft.world.explosion.Explosion.DestructionType;
import net.minecraft.world.explosion.ExplosionBehavior;

public class OverloadedElementalReaction extends ElementalReaction {
	public OverloadedElementalReaction() {
		super(
			new ElementalReactionSettings("Overloaded", OriginsGenshin.identifier("overloaded"), OriginsGenshinParticleFactory.OVERLOADED)
				.setReactionCoefficient(1.0)
				.setAuraElement(Element.PYRO, 2)
				.setTriggeringElement(Element.ELECTRO, 3)
				.setAsReversable(true)
				.setAsAllowingChildElements(true)
		);
	}

	@Override
	protected void onReaction(LivingEntity entity, ElementalApplication auraElement, ElementalApplication triggeringElement, double reducedGauge, @Nullable LivingEntity origin) {
        final World world = entity.getWorld();
	
        if (world.isClient) return;

		final double x = entity.getX();
		final double y = entity.getY();
		final double z = entity.getZ();
		final float power = 3;
	
		final NonEntityDamagingExplosion explosion = new NonEntityDamagingExplosion(
			world,
			entity,
			world.getDamageSources().explosion(null),
			new ExplosionBehavior(),
			x,
			y,
			z,
			power,
			true,
			world.getGameRules().getBoolean(OriginsGenshin.OVERLOADED_EXPLOSIONS_DAMAGE_BLOCKS)
				? DestructionType.DESTROY
				: DestructionType.KEEP
		);
		
        explosion.collectBlocksAndPushEntities();
        explosion.affectWorld(world.isClient);

        //  Sync the explosion effect to the client if the explosion is created on the server
        if (!(world instanceof ServerWorld serverWorld)) return;

        if (!explosion.shouldDestroy()) explosion.clearAffectedBlocks();

		dealOverloadedDamage(entity);

        for (ServerPlayerEntity serverPlayerEntity : serverWorld.getPlayers()) {
            if (serverPlayerEntity.squaredDistanceTo(x, y, z) >= 4096.0) continue;

			serverPlayerEntity.networkHandler.sendPacket(
				new ExplosionS2CPacket(
					x, 
					y, 
					z, 
					power, 
					explosion.getAffectedBlocks(), 
					explosion
						.getAffectedPlayers()
						.get(serverPlayerEntity)
				)
			);
        }
	}

	private void dealOverloadedDamage(LivingEntity origin) {
		final double radius = 2.5;
		final World world = origin.getWorld();

		final float OverloadedDMG = OriginsGenshin.getLevelMultiplier(world) * 1.2f;
		
		for (LivingEntity target : world.getNonSpectatingEntities(LivingEntity.class, Box.of(origin.getLerpedPos(1f), radius * 2, radius * 2, radius * 2))) {
			if (!origin.canTarget(target)) continue;

			final ElementalApplication application = ElementalApplication.gaugeUnits(target, Element.PYRO, 0);
			final ElementalDamageSource source = new ElementalDamageSource(world.getDamageSources().generic(), application, "reactions:overloaded");
			
			final boolean inCircleRadius = origin.squaredDistanceTo(target) <= (radius * radius);
			final ElementComponent component = ElementComponent.KEY.get(target);

			if (inCircleRadius && component.hasElementalApplication(Element.HYDRO)) target.damage(source, OverloadedDMG);
		}
	}
}
