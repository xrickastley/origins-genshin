package io.github.xrickastley.originsgenshin.element.reaction;

import java.util.Set;

import io.github.xrickastley.originsgenshin.element.ElementalApplication;
import io.github.xrickastley.originsgenshin.entity.CrystallizeShardEntity;
import io.github.xrickastley.originsgenshin.factory.OriginsGenshinEntities;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.LivingEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public abstract sealed class AbstractCrystallizeElementalReaction
	extends ElementalReaction
	permits PyroCrystallizeElementalReaction, HydroCrystallizeElementalReaction, ElectroCrystallizeElementalReaction, CryoCrystallizeElementalReaction, FrozenCrystallizeElementalReaction
{
	private static final Set<Block> AIR_BLOCKS = Set.of(Blocks.AIR, Blocks.CAVE_AIR, Blocks.VOID_AIR);

	AbstractCrystallizeElementalReaction(ElementalReactionSettings settings) {
		super(settings);
	}

	@Override
	protected void onReaction(LivingEntity entity, ElementalApplication auraElement, ElementalApplication triggeringElement, double reducedGauge, @Nullable LivingEntity origin) {
		final World world = entity.getWorld();

		if (!(world instanceof final ServerWorld serverWorld)) return;

		final Vec3d spawnPos = this.clampToGround(entity.getWorld(), this.toAbsolutePos(entity, new Vec3d(0, 0, 1)));
		final CrystallizeShardEntity crystallizeShard = new CrystallizeShardEntity(OriginsGenshinEntities.CRYSTALLIZE_SHARD, serverWorld, this.getAuraElement(), origin);

		crystallizeShard.setPosition(spawnPos);
		serverWorld.spawnNewEntityAndPassengers(crystallizeShard);
	}

	// Taken from LookingPosArgument#toAbsolutePos
	private Vec3d toAbsolutePos(final LivingEntity entity, final Vec3d lookingPos) {
		final Vec2f vec2f = entity.getRotationClient();
		final Vec3d vec3d = entity.getPos();

		float f = MathHelper.cos((vec2f.y + 90.0F) * 0.017453292F);
		float g = MathHelper.sin((vec2f.y + 90.0F) * 0.017453292F);
		float h = MathHelper.cos(-vec2f.x * 0.017453292F);
		float i = MathHelper.sin(-vec2f.x * 0.017453292F);
		float j = MathHelper.cos((-vec2f.x + 90.0F) * 0.017453292F);
		float k = MathHelper.sin((-vec2f.x + 90.0F) * 0.017453292F);
		Vec3d vec3d2 = new Vec3d(f * h, i, g * h);
		Vec3d vec3d3 = new Vec3d(f * j, k, g * j);
		Vec3d vec3d4 = vec3d2.crossProduct(vec3d3).multiply(-1.0);
		double d = vec3d2.x * lookingPos.z + vec3d3.x * lookingPos.y + vec3d4.x * lookingPos.x;
		double e = vec3d2.y * lookingPos.z + vec3d3.y * lookingPos.y + vec3d4.y * lookingPos.x;
		double l = vec3d2.z * lookingPos.z + vec3d3.z * lookingPos.y + vec3d4.z * lookingPos.x;
		return new Vec3d(vec3d.x + d, vec3d.y + e, vec3d.z + l);
   	}

	private Vec3d clampToGround(World world, Vec3d pos) {
		final BlockPos initialBlockPos = new BlockPos((int) pos.x, (int) pos.y, (int) pos.z);
		BlockPos blockPos = initialBlockPos;
		BlockState blockState = world.getBlockState(blockPos);

		while (blockPos.getY() >= world.getBottomY()) {
			blockPos = blockPos.add(0, -1, 0);

			if (AIR_BLOCKS.contains(blockState.getBlock())) continue;

			final int diff = initialBlockPos
				.add(blockPos.getX(), blockPos.getY(), blockPos.getZ())
				.getY();

			return initialBlockPos
				.add(0, -diff + 1, 0) // offset by 1 since it'll be underground.
				.toCenterPos();
		}

		return initialBlockPos.toCenterPos();
	}
}
