package AutoplayAddon.AutoPlay.Actions;

import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import static meteordevelopment.meteorclient.MeteorClient.mc;

import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class MineUtil {
    private static CompletableFuture<Void> tickEventFuture;
    private BlockPos targetBlockPos;

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (tickEventFuture != null && targetBlockPos != null) {
            World world = mc.world;
            if (world != null && world.getBlockState(targetBlockPos).isAir()) {
                tickEventFuture.complete(null);
            } else {
                mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, targetBlockPos, Direction.UP));
            }
        }
    }

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


    public void mine(BlockPos blockPos) {
        this.targetBlockPos = blockPos;
        ChatUtils.info("Mining block at " + targetBlockPos.toShortString());
        mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.START_DESTROY_BLOCK, targetBlockPos, Direction.UP));
        MeteorClient.EVENT_BUS.subscribe(this);
        tickEventFuture = new CompletableFuture<>();
        try {
            tickEventFuture.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        MeteorClient.EVENT_BUS.unsubscribe(this);
    }
}

