package AutoplayAddon.utils;

import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.Material;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class MineUtil {
    private static final MinecraftClient mc = MinecraftClient.getInstance();
    private CompletableFuture<Void> tickEventFuture;
    private BlockPos targetBlockPos;
    //private Direction targetDirection;

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
        //this.targetDirection = calculateTargetDirection();
        ChatUtils.info("Mining block at " + targetBlockPos.toShortString());
        mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.START_DESTROY_BLOCK, targetBlockPos, Direction.UP));
        tickEventFuture = new CompletableFuture<>();
        try {
            tickEventFuture.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }

    private Direction calculateTargetDirection() {
        Vec3d playerPos = mc.player.getPos();
        Vec3d targetPos = Vec3d.ofCenter(targetBlockPos);
        Vec3d directionVector = playerPos.subtract(targetPos).normalize(); // Reverse the direction vector

        double bestDotProduct = -1;
        Direction bestDirection = null;

        for (Direction direction : Direction.values()) {
            Vec3d vecDirection = Vec3d.of(direction.getVector()).normalize();
            double dotProduct = vecDirection.dotProduct(directionVector);
            if (dotProduct > bestDotProduct) {
                bestDotProduct = dotProduct;
                bestDirection = direction;
            }
        }

        return bestDirection;
    }

}
