package AutoplayAddon.AutoPlay.Locator;
import AutoplayAddon.AutoplayAddon;
import AutoplayAddon.Tracker.BlockCache;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import net.minecraft.block.Block;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import static meteordevelopment.meteorclient.MeteorClient.mc;

public class AirGapFinder {
    static BlockCache blockCache = AutoplayAddon.blockCache;
    public static List<Vec3d> findClosestValidStandingPos(List<Block> targetBlocks, double maxAirGapDistance) {
        BlockPos playerPos = mc.player.getBlockPos();
        BlockCache blockCache = AutoplayAddon.blockCache;

        // Get a list of positions of target blocks
        List<BlockPos> filteredPositions = blockCache.blockMap.entrySet().stream()
            .filter(entry -> targetBlocks.contains(entry.getValue()))
            .map(entry -> entry.getKey())
            .collect(Collectors.toList());

        // Sort positions from nearest to furthest
        filteredPositions.sort(Comparator.comparingInt(pos -> pos.getManhattanDistance(playerPos)));

        // Iterate through the sorted list of positions
        for (BlockPos currentPos : filteredPositions) {
            Vec3d airGapPos = findAirGapNearBlock(currentPos, maxAirGapDistance);
            if (airGapPos != null) {
                ChatUtils.info("Found");
                List<Vec3d> result = new ArrayList<>();
                result.add(new Vec3d(currentPos.getX(), currentPos.getY(), currentPos.getZ()));
                result.add(airGapPos);
                return result;
            }
        }
        ChatUtils.info("Didn't find");
        return null;
    }

    public static Vec3d findAirGapNearBlock(BlockPos targetPos, double maxAirGapDistance) {
        BlockPos closestValidPos = null;
        double minSquaredDistance = maxAirGapDistance * maxAirGapDistance; // Use squared distances to avoid sqrt

        for (BlockPos pos : BlockPos.iterate(targetPos.add((int) -maxAirGapDistance, (int) -maxAirGapDistance, (int) -maxAirGapDistance),
            targetPos.add((int) maxAirGapDistance, (int) maxAirGapDistance, (int) maxAirGapDistance))) {

            double squaredDistance = squaredDistanceBetweenBlockPos(targetPos, pos);

            if (squaredDistance > minSquaredDistance) {
                continue; // Skip if already beyond current closest distance
            }

            if (!blockCache.isBlockAt(pos) && !blockCache.isBlockAt(pos.add(0, 1, 0))) {
                if (squaredDistance < minSquaredDistance) {
                    closestValidPos = pos.toImmutable();
                    minSquaredDistance = squaredDistance;
                }
            }
        }

        if (closestValidPos == null) {
            return null;
        }

        double offsetX = closestValidPos.getX() < targetPos.getX() ? 0.7 : (closestValidPos.getX() > targetPos.getX() ? 0.3 : 0.5);
        double offsetZ = closestValidPos.getZ() < targetPos.getZ() ? 0.7 : (closestValidPos.getZ() > targetPos.getZ() ? 0.3 : 0.5);

        return new Vec3d(closestValidPos.getX() + offsetX, closestValidPos.getY(), closestValidPos.getZ() + offsetZ);
    }

    private static double squaredDistanceBetweenBlockPos(BlockPos pos1, BlockPos pos2) {
        double dx = pos1.getX() - pos2.getX();
        double dy = pos1.getY() - pos2.getY();
        double dz = pos1.getZ() - pos2.getZ();
        return dx * dx + dy * dy + dz * dz;
    }
}
