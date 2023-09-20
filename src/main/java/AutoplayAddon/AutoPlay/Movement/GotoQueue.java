package AutoplayAddon.AutoPlay.Movement;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import AutoplayAddon.Mixins.ClientConnectionInvokerMixin;
import AutoplayAddon.Tracker.ServerSideValues;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.network.encryption.NetworkEncryptionUtils;
import net.minecraft.network.message.LastSeenMessagesCollector;
import net.minecraft.network.message.MessageBody;
import net.minecraft.network.message.MessageSignatureData;
import net.minecraft.network.packet.c2s.play.ChatMessageC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.math.Vec3d;
import static meteordevelopment.meteorclient.MeteorClient.mc;



public class GotoQueue extends Movement {
    static CompletableFuture<Void> tickEventFuture;
    static double y;
    static int totalPackets, stage, packetsRequired;
    public static List<Teleport> tasks = new ArrayList<>();
    @EventHandler()
    private static void onPreTick(TickEvent.Pre event) {
        if (mc.player == null) return;
        if (AutoSetPosition && !closeBy(mc.player.getPos(), currentPosition)) {
            mc.player.setPosition(to);
            mc.player.setVelocity(Vec3d.ZERO);
        }
        if (!currentlyMoving) return;
        ChatUtils.info("delta is " + ServerSideValues.delta());
        if (teleport()) {
            currentlyMoving = false;
            tickEventFuture.complete(null);
        }
    }

    private static void calculatePackets() {
        packetsRequired = 0;
        for (Teleport task : tasks) {
            int packets = task.getPacketsRequired();
            if (packets > packetsRequired) {
                packetsRequired = packets;
            }
        }
        totalPackets = packetsRequired + tasks.size();
    }
    private static Boolean teleport() {
        calculatePackets();
        if (!(ServerSideValues.delta() >= totalPackets)) return false;
        Boolean setTo20 = false;
        if ((ServerSideValues.allowedPlayerTicks - totalPackets) < 20) {
            setTo20 = true;
        }
        while (packetsRequired > ServerSideValues.i2) {
            PlayerMoveC2SPacket packet;
            if (setTo20) {
                packet = new PlayerMoveC2SPacket.OnGroundOnly(true);
            } else {
                packet = new PlayerMoveC2SPacket.Full(currentPosition.x, currentPosition.y, currentPosition.z, yaw, pitch, true);
            }
            ServerSideValues.HandleMovePacketSafe(packet);
            ((ClientConnectionInvokerMixin) mc.getNetworkHandler().getConnection())._sendImmediately(packet, null);
        }
        for (Teleport task : tasks) {
            task.execute(setTo20);
        }
        ChatUtils.info("teleported with setTo20 " + setTo20 + " and packets required " + packetsRequired + " and total packets " + totalPackets);
        return true;
    }


    private static Vec3d getStage(Vec3d from, Vec3d to, int stage) {
        if (stage == 1) return new Vec3d(from.getX(), y, from.getZ());
        if (stage == 2) return new Vec3d(to.getX(), y, to.getZ());
        if (stage == 3) return new Vec3d(to.getX(), to.getY(), to.getZ());
        return new Vec3d(from.getX(), from.getY(), from.getZ());
    }


    public static void setPos(Vec3d pos) {
        currentlyMoving = false;
        tasks.clear();
        to = pos;
        if (closeBy(currentPosition, to)) return;
        y = CanTeleport.searchY(currentPosition, to);
        if (y == -1337) {
            y = currentPosition.y;
            stage = 2;
        } else {
            stage = 1;
        }
        Vec3d currentTempPos = currentPosition;
        while (true) {
            if (stage == 4) {
                break;
            }
            if (CanTeleport.lazyCheck(currentTempPos, to)) {
                tasks.add(new Teleport(to, currentTempPos, tasks.size()));
                break;
            }
            Vec3d newPos = getStage(currentTempPos, to, stage);
            tasks.add(new Teleport(newPos, currentTempPos, tasks.size()));
            currentTempPos = newPos;
            stage++;
        }
        calculatePackets();
        ChatUtils.info("we need to send a total of " + totalPackets + " packets and i needs to be " + packetsRequired );
        if (teleport()) return;
        ChatUtils.info("allowedplayerticks is too low");
        currentlyMoving = true;
        try {
            tickEventFuture.get();
        } catch (InterruptedException | ExecutionException e) {
            ChatUtils.error("goto utils interupped " + e);
        }
    }
}
