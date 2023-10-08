package AutoplayAddon.AutoPlay.Locator;

import AutoplayAddon.AutoplayAddon;
import AutoplayAddon.Tracker.BlockCache;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.item.Item;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.util.*;

import static meteordevelopment.meteorclient.MeteorClient.mc;

public class CanPickUpTest {
    static BlockCache blockCache = AutoplayAddon.blockCache;

    public static List<Vec3d> findCollectableItem(List<Item> targetItems) {
        List<Block> targetBlocks = new ArrayList<>();

        for (Item item : targetItems) {
            if (Block.getBlockFromItem(item) != Blocks.AIR) {
                targetBlocks.add(Block.getBlockFromItem(item));
            }
        }

        if (targetBlocks.isEmpty()) {
            return null;
        }

        return findCollectableBlock(targetBlocks);
    }


    public static List<Vec3d> findCollectableBlock(List<Block> targetBlocks) {
        BlockPos playerPos = mc.player.getBlockPos();
        int maxDistance = 100; // You can adjust this value based on your needs
        for (int distance = 1; distance <= maxDistance; distance++) {
            for (int dx = -distance; dx <= distance; dx++) {
                for (int dy = -distance; dy <= distance; dy++) {
                    for (int dz = -distance; dz <= distance; dz++) {
                        if (Math.abs(dx) != distance && Math.abs(dy) != distance && Math.abs(dz) != distance) {
                            continue; // This ensures we're only checking the outer layer of blocks at each distance
                        }
                        BlockPos checkPos = playerPos.add(dx, dy, dz);
                        Block checkBlock = mc.world.getBlockState(checkPos).getBlock();
                        if (targetBlocks.contains(checkBlock)) {
                            if (!blockCache.isBlockAt(checkPos.add(0, 1, 0)) || !blockCache.isBlockAt(checkPos.add(0, -1, 0)) || hasAirAdjacent(checkPos)) {
                                Vec3d airGapPos = AirGapFinder.findAirGapNearBlock(checkPos, 5);
                                if (airGapPos != null) {
                                    return List.of(new Vec3d(checkPos.getX(), checkPos.getY(), checkPos.getZ()), airGapPos);
                                }
                            }
                        }
                    }
                }
            }
        }
        return null;
    }


    public static boolean hasAirAdjacent(BlockPos pos) {
        // use blockpos.add because its faster than blockpos.up or blockpos.down etc
        for (BlockPos blockPos : ValidPickupPoint.getSurroundingBlocks(pos)) {
            if ((!blockCache.isBlockAt(blockPos.add(0, 1, 0)) && !blockCache.isBlockAt(blockPos)) ||
                (!blockCache.isBlockAt(blockPos.add(0, -1, 0)) && !blockCache.isBlockAt(blockPos.add(0, -2, 0)))) {
                return true;
            }
        }
        return false;
    }



}


