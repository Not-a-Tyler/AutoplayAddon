package AutoplayAddon.utils;

import net.minecraft.block.Blocks;
import net.minecraft.item.Item;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.block.Block;
import net.minecraft.client.MinecraftClient;
import meteordevelopment.meteorclient.utils.player.PlayerUtils;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import java.util.Comparator;
import java.util.ArrayList;
import java.util.List;

public class CanPickUpTest {
    private static final MinecraftClient mc = MinecraftClient.getInstance();

    public static List<Vec3d> findCollectableItem(List<Item> targetItems, double searchRadius) {
        List<Block> targetBlocks = new ArrayList<>();

        for (Item item : targetItems) {
            if (Block.getBlockFromItem(item) != Blocks.AIR) {
                targetBlocks.add(Block.getBlockFromItem(item));
            }
        }

        if (targetBlocks.isEmpty()) {
            return null;
        }

        return findCollectableBlock(targetBlocks, searchRadius);
    }



    public static List<Vec3d> findCollectableBlock(List<Block> targetBlocks, double searchRadius) {
        World world = mc.player.getEntityWorld();
        BlockPos playerPos = mc.player.getBlockPos();
        List<BlockPos> validBlockPositions = new ArrayList<>();

        for (double x = -searchRadius; x <= searchRadius; x++) {
            for (double y = -searchRadius; y <= searchRadius; y++) {
                for (double z = -searchRadius; z <= searchRadius; z++) {
                    double distanceSquared = x * x + y * y + z * z;
                    if (distanceSquared <= searchRadius * searchRadius) {
                        BlockPos currentPos = playerPos.add((int) x, (int) y, (int) z);
                        Block currentBlock = world.getBlockState(currentPos).getBlock();

                        if (targetBlocks.contains(currentBlock)) {
                            boolean hasAirAdjacent = hasAirAdjacent(world, currentPos);
                            boolean hasAirBlockAboveOrBelow = world.getBlockState(currentPos.up()).getBlock() == Blocks.AIR || world.getBlockState(currentPos.down()).getBlock() == Blocks.AIR;

                            if (hasAirAdjacent || hasAirBlockAboveOrBelow) {
                                validBlockPositions.add(currentPos);
                            }
                        }
                    }
                }
            }
        }

        if (validBlockPositions.isEmpty()) {
            return null;
        }

        validBlockPositions.sort(Comparator.comparingDouble(blockPos -> PlayerUtils.distanceTo(blockPos)));

        BlockPos closestBlockPos = validBlockPositions.get(0);
        Vec3d airGapPos = AirGapFinder.findAirGapNearBlock(closestBlockPos, 5);
        if (airGapPos != null) {
            List<Vec3d> result = new ArrayList<>();
            ChatUtils.info("AirGapPos: " + airGapPos);
            result.add(closestBlockPos.toCenterPos());
            result.add(airGapPos);
            return result;
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


