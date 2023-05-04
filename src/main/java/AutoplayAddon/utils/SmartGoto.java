package AutoplayAddon.utils;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import net.minecraft.block.Block;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;


import java.util.List;

public class SmartGoto {
    private static final GotoUtil gotoUtil = new GotoUtil();
    private static final MinecraftClient mc = MinecraftClient.getInstance();

    public static Vec3d gotoblock(List<Block> targetBlocks) {
        ChatUtils.info("Searching for blocks " + targetBlocks.toString());
        List<BlockPos> targetBlockPositions = GetLocUtil.findBlocks(targetBlocks, 5);
        if (targetBlockPositions.size() > 0) {
            return targetBlockPositions.get(0).toCenterPos();
        } else {
            ChatUtils.info("No target blocks found within 5 blocks.");
        }
        //Vec3d airGapPos = AirGapFinder.findClosestValidPos(targetBlocks, 100, 5);
        List<Vec3d> collectableBlock = AirGapFinder.findClosestValidPos(targetBlocks, 100, 5);
        Vec3d currentPos = collectableBlock.get(0);
        Vec3d airGapPos = collectableBlock.get(1);
        ChatUtils.info("Found air gap at: " + airGapPos.toString());
        if (airGapPos != null) {
            MeteorClient.EVENT_BUS.subscribe(gotoUtil);
            gotoUtil.moveto(airGapPos.getX(), airGapPos.getY(), airGapPos.getZ());
            MeteorClient.EVENT_BUS.unsubscribe(gotoUtil);
            return currentPos;
        } else {
            ChatUtils.info("No target blocks found within the search radius.");
            return null;
        }
    }
    public static void stop() {
        MeteorClient.EVENT_BUS.unsubscribe(gotoUtil);
    }
}
