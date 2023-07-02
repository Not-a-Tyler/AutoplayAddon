package AutoplayAddon.AutoPlay.Locator;

import AutoplayAddon.AutoplayAddon;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.item.Item;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
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
        World world = mc.player.getEntityWorld();
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

            boolean hasAirAdjacent = hasAirAdjacent(world, currentPos);
            boolean hasAirBlockAboveOrBelow = world.getBlockState(currentPos.up()).getBlock() == Blocks.AIR || world.getBlockState(currentPos.down()).getBlock() == Blocks.AIR;
            if (hasAirAdjacent || hasAirBlockAboveOrBelow) {
                Vec3d airGapPos = AirGapFinder.findAirGapNearBlock(currentPos, 5);
                if (airGapPos != null) {
                    List<Vec3d> result = new ArrayList<>();
                    ChatUtils.info("AirGapPos: " + airGapPos);
                    result.add(new Vec3d(currentPos.getX(), currentPos.getY(), currentPos.getZ())); // Convert BlockPos to Vec3d
                    result.add(airGapPos);
                    return result;
                }
            }
        }
        return null;
    }







    public static boolean hasAirAdjacent(World world, BlockPos pos) {
        for (BlockPos blockPos : ValidPickupPoint.getSurroundingBlocks(pos)) {
            if ((world.getBlockState(blockPos).getBlock() == Blocks.AIR && world.getBlockState(blockPos.up()).getBlock() == Blocks.AIR) ||
                (world.getBlockState(blockPos.down()).getBlock() == Blocks.AIR && world.getBlockState(blockPos.down(2)).getBlock() == Blocks.AIR) ||
                (world.getBlockState(blockPos.down()).getBlock() == Blocks.AIR && world.getBlockState(blockPos).getBlock() == Blocks.AIR)) {
                return true;
            }
        }
        return false;
    }


}


