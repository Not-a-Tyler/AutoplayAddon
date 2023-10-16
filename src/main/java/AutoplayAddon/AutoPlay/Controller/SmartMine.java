package AutoplayAddon.AutoPlay.Controller;
import AutoplayAddon.AutoPlay.Movement.GotoUtil;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.utils.player.PlayerUtils;
import AutoplayAddon.AutoPlay.Locator.CanPickUpTest;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.item.Item;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.util.math.BlockPos;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import meteordevelopment.meteorclient.utils.player.ChatUtils;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import static meteordevelopment.meteorclient.MeteorClient.mc;

public class SmartMine {

    private static CompletableFuture<Void> tickEventFuture;
    private static BlockPos targetBlockPos;


    @EventHandler
    private void onRender(Render3DEvent event) {
        double x1 = targetBlockPos.getX();
        double y1 = targetBlockPos.getY();
        double z1 = targetBlockPos.getZ();
        double x2 = x1 + 1;
        double y2 = y1 + 1;
        double z2 = z1 + 1;
        event.renderer.box(x1, y1, z1, x2, y2, z2, new SettingColor(255, 0, 255, 15), new SettingColor(255, 0, 255, 15), ShapeMode.Both, 0);
    }

    private static int amount; // Added variable to keep track of the number of blocks mined

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (tickEventFuture != null && targetBlockPos != null) {
            World world = mc.world;
            if (world != null && world.getBlockState(targetBlockPos).isAir()) {
                tickEventFuture.complete(null);
                amount--;
                if (amount <= 0) {
                    ChatUtils.info("Finished mining the specified amount of blocks.");
                }
            } else {
                mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, targetBlockPos, Direction.UP));
            }
        }
    }


    public static void mineBlocks(List<Item> targetBlocks, int Amount) {
        amount = Amount; // Set the amount variable
        MeteorClient.EVENT_BUS.subscribe(new SmartMine());
        tickEventFuture = new CompletableFuture<>();
        while (amount > 0) {
            List<Vec3d> collectableBlock = CanPickUpTest.findCollectableItem(targetBlocks);
            if (collectableBlock == null) {
                ChatUtils.info("No more target blocks found within the search radius.");
                break;
            }
            Vec3d targetpos = collectableBlock.get(0);
            Vec3d airGapPos = collectableBlock.get(1);
            mine(targetpos, airGapPos);
            ChatUtils.info("done mining");
        }
        MeteorClient.EVENT_BUS.unsubscribe(new SmartMine());
        if (amount > 0) {
            ChatUtils.info("No more target blocks found within the search radius.");
        }
    }


    private static void mine(Vec3d targetCenterPos, Vec3d airGapPos) {
        if (PlayerUtils.distanceTo(targetCenterPos) > 5) {
            GotoUtil.setPos(airGapPos, false, true, true);
        }
        targetBlockPos = BlockPos.ofFloored(targetCenterPos);
        ChatUtils.info("Mining block at " + targetBlockPos.toShortString());
        mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.START_DESTROY_BLOCK, targetBlockPos, Direction.UP));

    }

}
