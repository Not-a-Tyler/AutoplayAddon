package AutoplayAddon.utils;
import meteordevelopment.meteorclient.utils.player.PlayerUtils;
import net.minecraft.item.Item;
import net.minecraft.util.math.BlockPos;
import java.util.List;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import net.minecraft.util.math.Vec3d;

public class SmartMine {


    public static void mineBlocks(List<Item> targetBlocks) {
        List<Vec3d> collectableBlock = CanPickUpTest.findCollectableItem(targetBlocks, 100);
        if (collectableBlock != null) {
            Vec3d targetpos = collectableBlock.get(0);
            Vec3d airGapPos = collectableBlock.get(1);
            if (PlayerUtils.distanceTo(targetpos) > 5) {
                new GotoUtil().moveto(airGapPos.getX(), airGapPos.getY(), airGapPos.getZ());
            }
            ChatUtils.info("Mining target block at: " + targetpos.getX() + " " + targetpos.getY() + " " + targetpos.getZ());
            new MineUtil().mine(BlockPos.ofFloored(targetpos));
        } else {
            ChatUtils.info("No target blocks found within the search radius.");
        }
    }

}
