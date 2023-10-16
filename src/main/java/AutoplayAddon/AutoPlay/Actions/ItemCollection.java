package AutoplayAddon.AutoPlay.Actions;
import static meteordevelopment.meteorclient.MeteorClient.mc;

import AutoplayAddon.AutoPlay.Locator.GetLocUtil;
import AutoplayAddon.AutoPlay.Locator.ValidPickupPoint;
import AutoplayAddon.AutoPlay.Movement.GotoUtil;
import net.minecraft.item.Item;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.BlockPos;
import meteordevelopment.meteorclient.utils.player.ChatUtils;


import java.util.List;

public class ItemCollection {
    public static boolean collect(List<Item> targetItems) {
        List<BlockPos> itemPositions = GetLocUtil.findItemEntities(targetItems, 100);
        if (itemPositions.isEmpty()) {
            return false;
        }
        for (BlockPos itemPosition : itemPositions) {
            ChatUtils.info("Item found at: " + itemPosition.getX() + " " + itemPosition.getY() + " " + itemPosition.getZ());
            Vec3d test = ValidPickupPoint.findFitSpot(mc.world, itemPosition);
            if (test != null) {
                GotoUtil.setPos(test, false, true, true);
                return true;
            }
        }
        return false;
    }

}
