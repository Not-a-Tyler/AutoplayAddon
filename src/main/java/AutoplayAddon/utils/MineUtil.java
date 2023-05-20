package AutoplayAddon.utils;

import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
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

