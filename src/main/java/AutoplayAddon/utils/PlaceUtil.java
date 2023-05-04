package AutoplayAddon.utils;

import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.meteorclient.utils.player.FindItemResult;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.meteorclient.utils.world.BlockUtils;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.Item;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.Hand;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class PlaceUtil {
    private static final MinecraftClient mc = MinecraftClient.getInstance();


    List<Block> targetBlocks = Collections.singletonList(Blocks.AIR);

    GetLocUtil getLocUtil = new GetLocUtil();


    public static void randomplace(Block block) {
        Item item = block.asItem();
        ChatUtils.info("Looking for: " + item);
        FindItemResult result = InvUtils.find(item);
        int craftedSlot = result.slot();
        ChatUtils.info("Crafted slot: " + craftedSlot);
        List<Block> targetBlocks = Arrays.asList(Blocks.AIR);
        List<BlockPos> loc = GetLocUtil.findBlocks(targetBlocks, 5);
        BlockPos loc1 = loc.get(0);
        ChatUtils.info("Placing at: " + loc1);
        BlockUtils.place(loc1, Hand.MAIN_HAND, craftedSlot, false, 0, true, true, false);
        //placeBlock(loc1, craftedSlot);
    }

    //public static void placeBlock(BlockPos pos, int slot) {
    //    Direction side = null;
    //    Vec3d hitVec = null;

        //for (Direction direction : Direction.values()) {
        //    BlockPos neighbor = pos.offset(direction);
        //    if (mc.world.getBlockState(neighbor).isAir() || mc.world.getBlockState(neighbor).getMaterial().isReplaceable()) {
        //        side = direction;
        //        hitVec = new Vec3d(neighbor.getX() + 0.5, neighbor.getY() + 0.5, neighbor.getZ() + 0.5).add(new Vec3d(side.getVector().getX(), side.getVector().getY(), side.getVector().getZ()).multiply(0.5));
        //        break;
        //    }
        //}

        //if (side == null || hitVec == null) {
        //    return;
        //}

    //    BlockUtils.place(pos, Hand.MAIN_HAND, slot, false, 0, true, true, false);
    //}

}
