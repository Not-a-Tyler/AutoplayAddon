package AutoplayAddon.utils;
import meteordevelopment.meteorclient.utils.player.PlayerUtils;
import net.minecraft.block.Block;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.Item;
import net.minecraft.util.math.BlockPos;

import meteordevelopment.meteorclient.MeteorClient;
import java.util.List;

import meteordevelopment.meteorclient.utils.player.ChatUtils;
import net.minecraft.util.math.Vec3d;

public class SmartMine {
    private static final MinecraftClient mc = MinecraftClient.getInstance();
    GotoUtil gotoUtil = new GotoUtil();
    MineUtil mineUtil = new MineUtil();

    public void processBlocks(List<Item> targetBlocks) {



        List<Vec3d> collectableBlock = CanPickUpTest.findCollectableItem(targetBlocks, 100);


        if (collectableBlock != null) {
            Vec3d targetpos = collectableBlock.get(0);
            Vec3d airGapPos = collectableBlock.get(1);
            if (PlayerUtils.distanceTo(targetpos) > 5) {
                MeteorClient.EVENT_BUS.subscribe(gotoUtil);
                gotoUtil.moveto(airGapPos.getX(), airGapPos.getY(), airGapPos.getZ());
                MeteorClient.EVENT_BUS.unsubscribe(gotoUtil);
            }

            MeteorClient.EVENT_BUS.subscribe(mineUtil);
            ChatUtils.info("Mining target block at: " + targetpos.getX() + " " + targetpos.getY() + " " + targetpos.getZ());
            mineUtil.mine(BlockPos.ofFloored(targetpos));
            MeteorClient.EVENT_BUS.unsubscribe(mineUtil);
        } else {
            ChatUtils.info("No target blocks found within the search radius.");
        }
    }
    public void stop() {
        SmartGoto.stop();
        MeteorClient.EVENT_BUS.unsubscribe(mineUtil);
    }

}
