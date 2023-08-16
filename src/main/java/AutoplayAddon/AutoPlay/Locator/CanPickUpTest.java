package AutoplayAddon.AutoPlay.Locator;

import AutoplayAddon.AutoplayAddon;
import AutoplayAddon.Tracker.BlockCache;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.item.Item;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.*;
import java.util.stream.Collectors;

import static meteordevelopment.meteorclient.MeteorClient.mc;

public class CanPickUpTest {

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
        BlockCache blockCache = AutoplayAddon.blockCache;

        List<BlockCache.BlockData> collectableBlocks = new ArrayList<>();
        for (Block block : targetBlocks) {
            if (blockCache.blockMap.containsKey(block)) {
                collectableBlocks.addAll(blockCache.blockMap.get(block));
            }
        }
        ChatUtils.info(String.valueOf(System.currentTimeMillis()));
        collectableBlocks.sort(Comparator.comparingInt(blockData -> blockData.getPos().getManhattanDistance(playerPos)));
        ChatUtils.info(String.valueOf(System.currentTimeMillis()));
        for (BlockCache.BlockData blockData : collectableBlocks) {
            BlockPos pos = blockData.getPos();
            if (isAirOrNonSolid(pos.add(0, 1, 0)) || isAirOrNonSolid(pos.add(0, -1, 0)) || hasAirAdjacent(pos)) {
                Vec3d airGapPos = AirGapFinder.findAirGapNearBlock(pos, 5);
                if (airGapPos != null) {
                    return List.of(new Vec3d(pos.getX(), pos.getY(), pos.getZ()), airGapPos);
                }
            }
        }
        ChatUtils.info(String.valueOf(System.currentTimeMillis()));
        return null;
    }

    public static boolean hasAirAdjacent(BlockPos pos) {
        // use blockpos.add because its faster than blockpos.up or blockpos.down etc
        for (BlockPos blockPos : ValidPickupPoint.getSurroundingBlocks(pos)) {
            if ((isAirOrNonSolid(blockPos.add(0, 1, 0)) && isAirOrNonSolid(blockPos)) ||
                (isAirOrNonSolid(blockPos.add(0, -1, 0)) && isAirOrNonSolid(blockPos.add(0, -2, 0)))) {
                return true;
            }
        }
        return false;
    }

    public static boolean isAirOrNonSolid(BlockPos pos) {
        return !AutoplayAddon.blockCache.solidBlocks.contains(pos);
    }


}


