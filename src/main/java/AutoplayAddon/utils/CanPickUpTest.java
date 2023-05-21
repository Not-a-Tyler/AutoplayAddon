package AutoplayAddon.utils;

import net.minecraft.block.Blocks;
import net.minecraft.item.Item;
import net.minecraft.util.math.BlockPos;
import static meteordevelopment.meteorclient.MeteorClient.mc;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.block.Block;
import meteordevelopment.meteorclient.utils.player.ChatUtils;

import java.util.ArrayList;
import java.util.List;

public class CanPickUpTest {

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
        for (int r = 0; r <= searchRadius; r++) {
            for (int dx = -r; dx <= r; dx++) {
                for (int dz = -r; dz <= r; dz++) {
                    if(Math.abs(dx) != r && Math.abs(dz) != r) continue;
                    for (int dy = -r; dy <= r; dy++) {
                        BlockPos currentPos = playerPos.add(dx, dy, dz);
                        Block currentBlock = world.getBlockState(currentPos).getBlock();
                        if (targetBlocks.contains(currentBlock)) {

                            boolean hasAirAdjacent = hasAirAdjacent(world, currentPos);
                            boolean hasAirBlockAboveOrBelow = world.getBlockState(currentPos.up()).getBlock() == Blocks.AIR || world.getBlockState(currentPos.down()).getBlock() == Blocks.AIR;
                            if (hasAirAdjacent || hasAirBlockAboveOrBelow) {
                                Vec3d airGapPos = AirGapFinder.findAirGapNearBlock(currentPos, 5);
                                if (airGapPos != null) {
                                    List<Vec3d> result = new ArrayList<>();
                                    ChatUtils.info("AirGapPos: " + airGapPos);
                                    result.add(currentPos.toCenterPos());
                                    result.add(airGapPos);
                                    return result;
                                }
                            }

                        }
                    }
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


