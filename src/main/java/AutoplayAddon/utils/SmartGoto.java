package AutoplayAddon.utils;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import net.minecraft.block.Block;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import java.util.List;

public class SmartGoto {

    public static Vec3d gotoblock(List<Block> targetBlocks) {
        ChatUtils.info("Searching for blocks " + targetBlocks.toString());
        //List<BlockPos> targetBlockPositions = GetLocUtil.findBlocks(targetBlocks, 5);
        //if (targetBlockPositions.isEmpty()) {
        //    ChatUtils.info("No target blocks found within 5 blocks.");
        //} else {
        //    return targetBlockPositions.get(0).toCenterPos();
       // }
        List<Vec3d> collectableBlock = AirGapFinder.findClosestValidPos(targetBlocks, 100, 5);
        Vec3d currentPos = collectableBlock.get(0);
        Vec3d airGapPos = collectableBlock.get(1);
        ChatUtils.info("Found air gap at: " + airGapPos.toString());
        if (airGapPos != null) {
            new GotoUtil().moveto(airGapPos.getX(), airGapPos.getY(), airGapPos.getZ());
            return currentPos;
        } else {
            ChatUtils.info("No target blocks found within the search radius.");
            return null;
        }
    }
}
