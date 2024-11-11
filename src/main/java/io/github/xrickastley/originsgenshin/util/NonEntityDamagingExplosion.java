package io.github.xrickastley.originsgenshin.util;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import javax.annotation.Nullable;

import com.google.common.collect.Sets;

import io.github.xrickastley.originsgenshin.mixin.ExplosionAccessor;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

import net.minecraft.block.BlockState;
import net.minecraft.enchantment.ProtectionEnchantment;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.TntEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;
import net.minecraft.world.explosion.Explosion;
import net.minecraft.world.explosion.ExplosionBehavior;

/**
 * A class for explosions that don't damage entities.
 */
public class NonEntityDamagingExplosion extends Explosion {
    private World world;
    private double x;
    private double y;
    private double z;
    private @Nullable Entity entity;
    private float power;
    private ExplosionBehavior behavior;
    private ObjectArrayList<BlockPos> affectedBlocks;
    private Map<PlayerEntity, Vec3d> affectedPlayers;

	public NonEntityDamagingExplosion(final World world, @Nullable final Entity entity, final double x, final double y, final double z, final float power, final List<BlockPos> affectedBlocks) {
		super(world, entity, x, y, z, power, false, Explosion.DestructionType.DESTROY_WITH_DECAY, affectedBlocks);

		supplyFromAccessor();
	}
	
	public NonEntityDamagingExplosion(final World world, @Nullable final Entity entity, final double x, final double y, final double z, final float power, final boolean createFire, final Explosion.DestructionType destructionType, final List<BlockPos> affectedBlocks) {
		super(world, entity, x, y, z, power, createFire, destructionType);

		supplyFromAccessor();
	}

	public NonEntityDamagingExplosion(final World world, @Nullable final Entity entity, final double x, final double y, final double z, final float power, final boolean createFire, final Explosion.DestructionType destructionType) {
		super(world, entity, null, null, x, y, z, power, createFire, destructionType);

		supplyFromAccessor();
	}
	
	public NonEntityDamagingExplosion(final World world, @Nullable final Entity entity, @Nullable final DamageSource damageSource, @Nullable final ExplosionBehavior behavior, final double x, final double y, final double z, final float power, final boolean createFire, final Explosion.DestructionType destructionType) {
		super(world, entity, damageSource, behavior, x, y, z, power, createFire, destructionType);
		
		supplyFromAccessor();
	}

    /**
     * Another version of {@link Explosion#collectBlocksAndDamageEntities}, but pushes 
     * entities (applies velocity) instead of damaging them.
     */
    public void collectBlocksAndPushEntities() {
        this.world.emitGameEvent(this.entity, GameEvent.EXPLODE, new Vec3d(this.x, this.y, this.z));
        final Set<BlockPos> set = Sets.newHashSet();

        for (int j = 0; j < 16; ++j) {
            for (int k = 0; k < 16; ++k) {
                for (int l = 0; l < 16; ++l) {
                    if (j == 0 || j == 15 || k == 0 || k == 15 || l == 0 || l == 15) {
                        double d = j / 15.0f * 2.0f - 1.0f;
                        double e = k / 15.0f * 2.0f - 1.0f;
                        double f = l / 15.0f * 2.0f - 1.0f;
                        final double g = Math.sqrt(d * d + e * e + f * f);

                        d /= g;
                        e /= g;
                        f /= g;

                        float h = this.power * (0.7f + this.world.random.nextFloat() * 0.6f);
                        double m = this.x;
                        double n = this.y;
                        double o = this.z;
						
                        while (h > 0.0f) {
                            final BlockPos blockPos = BlockPos.ofFloored(m, n, o);
                            final BlockState blockState = this.world.getBlockState(blockPos);
                            final FluidState fluidState = this.world.getFluidState(blockPos);
                            
							if (!this.world.isInBuildLimit(blockPos)) break;
                            
                            final Optional<Float> optional = this.behavior.getBlastResistance(this, (BlockView) this.world, blockPos, blockState, fluidState);
                            
							if (optional.isPresent()) h -= (optional.get() + 0.3f) * 0.3f;

                            if (h > 0.0f && this.behavior.canDestroyBlock(this, (BlockView) this.world, blockPos, blockState, h)) {
                                set.add(blockPos);
                            }
                            m += d * 0.30000001192092896;
                            n += e * 0.30000001192092896;
                            o += f * 0.30000001192092896;
                            h -= 0.22500001f;
                        }
                    }
                }
            }
        }
       
		this.affectedBlocks.addAll((Collection<BlockPos>) set);

        final float q = this.power * 2.0f;
        int k = MathHelper.floor(this.x - q - 1.0);
        int l = MathHelper.floor(this.x + q + 1.0);
        final int r = MathHelper.floor(this.y - q - 1.0);
        final int s = MathHelper.floor(this.y + q + 1.0);
        final int t = MathHelper.floor(this.z - q - 1.0);
        final int u = MathHelper.floor(this.z + q + 1.0);

        final List<Entity> list = this.world.getOtherEntities(this.entity, new Box((double)k, (double)r, (double)t, (double)l, (double)s, (double)u));
        final Vec3d vec3d = new Vec3d(this.x, this.y, this.z);
        
		for (final Entity entity : list) {
            if (entity.isImmuneToExplosion()) continue;
            
            final double v = Math.sqrt(entity.squaredDistanceTo(vec3d)) / q;
            
			if (v > 1.0) continue;
            
            double w = entity.getX() - this.x;
            double x = ((entity instanceof TntEntity) ? entity.getY() : entity.getEyeY()) - this.y;
            double y = entity.getZ() - this.z;
            final double z = Math.sqrt(w * w + x * x + y * y);
            
			if (z == 0.0) continue;
            
            w /= z;
            x /= z;
            y /= z;

            final double aa = getExposure(vec3d, entity);
            final double ab = (1.0 - v) * aa;

			final double ac = entity instanceof final LivingEntity livingEntity
				? ProtectionEnchantment.transformExplosionKnockback(livingEntity, ab)
            	: ab;

            w *= ac;
            x *= ac;
            y *= ac;

            final Vec3d vec3d2 = new Vec3d(w, x, y);
			
            entity.setVelocity(entity.getVelocity().add(vec3d2));

            if (!(entity instanceof final PlayerEntity playerEntity)) continue;

            if (playerEntity.isSpectator() || (playerEntity.isCreative() && playerEntity.getAbilities().flying)) {
                continue;
            }

            this.affectedPlayers.put(playerEntity, vec3d2);
        }
    }
    
	private void supplyFromAccessor() {
		final ExplosionAccessor accessor = (ExplosionAccessor) this;

		this.world = accessor.getWorld();
		this.x = accessor.getX();
		this.y = accessor.getY();
		this.z = accessor.getZ();
		this.entity = accessor.getEntity();
		this.power = accessor.getPower();
		this.behavior = accessor.getBehavior();
		this.affectedBlocks = accessor.getAffectedBlocks();
		this.affectedPlayers = accessor.getAffectedPlayers();
	}
}
