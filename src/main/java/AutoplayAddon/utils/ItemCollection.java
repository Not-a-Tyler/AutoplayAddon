package AutoplayAddon.utils;
import meteordevelopment.meteorclient.MeteorClient;
import static meteordevelopment.meteorclient.MeteorClient.mc;
import net.minecraft.item.Item;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.BlockPos;
import meteordevelopment.meteorclient.utils.player.ChatUtils;


import java.util.List;

public class ItemCollection {
    public static void collect(List<Item> targetItems) {
        List<BlockPos> itemPositions = GetLocUtil.findItemEntities(targetItems, 100);
        if (itemPositions == null) {
            ChatUtils.info("No Item found within the search radius.");
        }
        for (BlockPos itemPosition : itemPositions) {
            ChatUtils.info("Item found at: " + itemPosition.getX() + " " + itemPosition.getY() + " " + itemPosition.getZ());
            Vec3d test = ValidPickupPoint.findFitSpot(mc.world, itemPosition);
            if (test == null) {
                ChatUtils.info("No Valid Pickup Point found");
            } else {
                ChatUtils.info("Valid Pickup Point: " + test);
                new GotoUtil().moveto(test.getX(), test.getY(), test.getZ());
            }
        }
    }

}
