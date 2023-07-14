package AutoplayAddon.AutoPlay.Locator;
import AutoplayAddon.AutoplayAddon;
import AutoplayAddon.Tracker.BlockCache;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import net.minecraft.block.Block;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import static meteordevelopment.meteorclient.MeteorClient.mc;

public class AirGapFinder {
    public static List<Vec3d> findClosestValidStandingPos(List<Block> targetBlocks, double maxAirGapDistance) {
        BlockPos playerPos = mc.player.getBlockPos();

        BlockCache blockCache = AutoplayAddon.blockCache; // Initialize the BlockCache. This could be also passed as an argument if it's being used elsewhere.

        // Filter cached blocks to only include target blocks
        List<BlockCache.BlockData> filteredBlocks = targetBlocks.stream()
            .flatMap(block -> blockCache.blockMap.getOrDefault(block, new ArrayList<>()).stream())
            .collect(Collectors.toList());

        // Sort filtered blocks from nearest to furthest
        Collections.sort(filteredBlocks, Comparator.comparingInt(blockData -> blockData.getPos().getManhattanDistance(playerPos)));

        // Iterate through the sorted list of blocks
        for (BlockCache.BlockData blockData : filteredBlocks) {
            BlockPos currentPos = blockData.getPos();
            Vec3d airGapPos = findAirGapNearBlock(currentPos, maxAirGapDistance);
            if (airGapPos != null) {
                ChatUtils.info("Foumd");
                List<Vec3d> result = new ArrayList<>();
                result.add(new Vec3d(currentPos.getX(), currentPos.getY(), currentPos.getZ())); // Convert BlockPos to Vec3d
                result.add(airGapPos);
                return result;
            }
        }
        ChatUtils.info("Dident find");
        return null;
    }


        public static Vec3d findAirGapNearBlock(BlockPos targetPos, double maxAirGapDistance) {
        World world = mc.player.getEntityWorld();
        BlockPos closestValidPos = null;
        double minDistance = Double.MAX_VALUE;

        for (BlockPos pos : BlockPos.iterate(targetPos.add((int) -maxAirGapDistance, (int) -maxAirGapDistance, (int) -maxAirGapDistance),
            targetPos.add((int) maxAirGapDistance, (int) maxAirGapDistance, (int) maxAirGapDistance))) {
            if (world.isAir(pos) && world.isAir(pos.up())) {
                double distance = distanceBetweenBlockPos(targetPos, pos);
                if (distance <= maxAirGapDistance && distance < minDistance) {
                    closestValidPos = pos.toImmutable();
                    minDistance = distance;
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

    private static double distanceBetweenBlockPos(BlockPos pos1, BlockPos pos2) {
        double dx = pos1.getX() - pos2.getX();
        double dy = pos1.getY() - pos2.getY();
        double dz = pos1.getZ() - pos2.getZ();
        return Math.sqrt(dx * dx + dy * dy + dz * dz);
    }
}
