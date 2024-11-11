package io.github.xrickastley.originsgenshin.mixin;

import java.util.Map;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.explosion.Explosion;
import net.minecraft.world.explosion.ExplosionBehavior;

@Mixin(Explosion.class)
public interface ExplosionAccessor {
    @Accessor
    public World getWorld();
    @Accessor
    public double getX();
    @Accessor
    public double getY();
    @Accessor
    public double getZ();
    @Accessor
    public Entity getEntity();
    @Accessor
    public float getPower();
    @Accessor
    public ExplosionBehavior getBehavior();
    @Accessor
    public ObjectArrayList<BlockPos> getAffectedBlocks();
    @Accessor
    public Map<PlayerEntity, Vec3d> getAffectedPlayers();
}
