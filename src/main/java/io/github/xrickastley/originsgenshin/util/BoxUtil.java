package io.github.xrickastley.originsgenshin.util;

import net.minecraft.util.math.Box;

public class BoxUtil {
	public static Box multiply(Box box, double factor) {
		final double centerX = (box.minX + box.maxX) / 2;
		final double centerY = (box.minY + box.maxY) / 2;
		final double centerZ = (box.minZ + box.maxZ) / 2;

		final double halfExtentX = ((box.maxX - box.minX) / 2) * factor;
		final double halfExtentY = ((box.maxY - box.minY) / 2) * factor;
		final double halfExtentZ = ((box.maxZ - box.minZ) / 2) * factor;

		return new Box(centerX - halfExtentX, centerY - halfExtentY, centerZ - halfExtentZ, centerX + halfExtentX, centerY + halfExtentY, centerZ + halfExtentZ);
	}

	public static boolean isColliding(Box box, Box other) {
		return box.minX <= other.maxX && box.maxX >= other.minX
			&& box.minY <= other.maxY && box.maxY >= other.minY
			&& box.minZ <= other.maxZ && box.maxZ >= other.minZ;
	}
}
