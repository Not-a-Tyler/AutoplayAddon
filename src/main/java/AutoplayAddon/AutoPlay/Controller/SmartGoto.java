package AutoplayAddon.AutoPlay.Controller;
import AutoplayAddon.AutoPlay.Locator.GetLocUtil;
import AutoplayAddon.AutoPlay.Movement.GotoQueue;
import AutoplayAddon.AutoPlay.Movement.GotoUtil;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import net.minecraft.block.Block;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import java.util.List;
import AutoplayAddon.AutoPlay.Locator.AirGapFinder;

public class SmartGoto {

    public static Vec3d gotoblock(List<Block> targetBlocks) {
        ChatUtils.info("Searching for blocks " + targetBlocks.toString());
        BlockPos targetBlockPositions = GetLocUtil.findBlocks(targetBlocks, 5);
        if (targetBlockPositions == null) {
            ChatUtils.info("No target blocks found within 5 blocks.");
        } else {
            return targetBlockPositions.toCenterPos();
        }
        List<Vec3d> collectableBlock = AirGapFinder.findClosestValidStandingPos(targetBlocks, 5);
        Vec3d currentPos = collectableBlock.get(0);
        Vec3d airGapPos = collectableBlock.get(1);
        ChatUtils.info("Found air gap at: " + airGapPos.toString());
        if (airGapPos != null) {
            GotoQueue.setPos(airGapPos);
            return currentPos;
        } else {
            ChatUtils.info("No target blocks found within the search radius.");
            return null;
        }
    }
}
