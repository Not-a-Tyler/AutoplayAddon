package AutoplayAddon.utils;
import meteordevelopment.meteorclient.MeteorClient;
import static meteordevelopment.meteorclient.MeteorClient.mc;
import net.minecraft.item.Item;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.BlockPos;
import meteordevelopment.meteorclient.utils.player.ChatUtils;


import java.util.List;

public class ItemCollection {
    private final GotoUtil gotoUtil = new GotoUtil();
    public void collect(List<Item> targetItems) {
        List<BlockPos> itemPositions = GetLocUtil.findItemEntities(targetItems, 100);
        for (BlockPos itemPosition : itemPositions) {
            ChatUtils.info("Wood found at: " + itemPosition.getX() + " " + itemPosition.getY() + " " + itemPosition.getZ());
            Vec3d test = ValidPickupPoint.findFitSpot(mc.world, itemPosition);
            if (test == null) {
                ChatUtils.info("No Valid Pickup Point found");
            } else {
                ChatUtils.info("Valid Pickup Point: " + test);
                MeteorClient.EVENT_BUS.subscribe(gotoUtil);
                gotoUtil.moveto(test.getX(), test.getY(), test.getZ());
                MeteorClient.EVENT_BUS.unsubscribe(gotoUtil);
            }
        }
        if (itemPositions == null) {
            ChatUtils.info("No Wood found within the search radius.");
        }
    }

}
