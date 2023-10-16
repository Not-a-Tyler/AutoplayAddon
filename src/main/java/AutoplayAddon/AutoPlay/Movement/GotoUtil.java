package AutoplayAddon.AutoPlay.Movement;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;

import AutoplayAddon.AutoPlay.Other.Packet;
import AutoplayAddon.AutoplayAddon;
import AutoplayAddon.Mixins.ClientConnectionInvokerMixin;
import AutoplayAddon.Tracker.ServerSideValues;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.orbit.EventPriority;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.math.Vec3d;
import static meteordevelopment.meteorclient.MeteorClient.mc;

public class GotoUtil extends Movement {
    static CompletableFuture<Void> tickEventFuture;
    static Boolean returningToStart = false, setPos = false;
    static List<TeleportTask> bigestPath = new ArrayList<>();
    public static List<TeleportTask> returnPath = new ArrayList<>();
    static List<TeleportTask> pathGoals = new ArrayList<>();
    @EventHandler(priority = EventPriority.LOWEST - 1)
    private static void onTick(TickEvent.Pre event) {
        if (mc.player == null) return;
        if (setPos && !closeBy(mc.player.getPos(), currentPosition)) mc.player.setPosition(currentPosition);
        if (currentlyMoving) attemptTeleport();
    }


    private synchronized static Boolean attemptTeleport() {

        int packetsRequired = Path.calculatePackets(bigestPath);

        int tempAllowed = ServerSideValues.predictallowedPlayerTicks();
        int tempI = ServerSideValues.i2;
        int sentTemp = 0;
        // we are going to do a basic simulatiom of the servers checks to see if we have allowedplayerticks high enough to teleport
        // we account for the packets required, we don't know if the player has moved
        int tempPackets;
        if (returningToStart) {
            tempPackets = returnPath.size() + packetsRequired + pathGoals.size();
        } else {
            tempPackets = packetsRequired + pathGoals.size();
        }
        ChatUtils.info("delta is " + ServerSideValues.delta() + " i2: " + ServerSideValues.i2 + " allowed " + ServerSideValues.predictallowedPlayerTicks() + " tempPackets: " + tempPackets);
        while (tempI < packetsRequired) {
            tempI++;
            sentTemp++;
            if (tempI > Math.max(tempAllowed, 5)) {
                return false;
            }
            if (tempAllowed > 20 || ServerSideValues.hasMoved) {
                tempAllowed -= 1;
            } else {
                tempAllowed = 20;
            }
        }
        // now we account for the tasks
        while (tempI < tempPackets) {
            tempI++;
            sentTemp++;
            if (tempI > Math.max(tempAllowed, 5)) {
                return false;
            }
            // we know that the task has movement so we can subtract one
            tempAllowed -= 1;
        }

        // return if our teleport will push us over the general packet limit
        if (!ServerSideValues.canSendPackets(sentTemp, System.nanoTime())) return false;

        // we are going to send the bypass packets
        while (packetsRequired > ServerSideValues.i2) {
            PlayerMoveC2SPacket packet;
            if (ServerSideValues.predictallowedPlayerTicks() > 20) {
                packet = new PlayerMoveC2SPacket.LookAndOnGround(mc.player.getYaw(), mc.player.getPitch(), true);
            } else {
                packet = new PlayerMoveC2SPacket.OnGroundOnly(true);
            }
            ServerSideValues.HandleMovePacketSafe(packet);
            Packet.packetQueue.add(packet);
        }

        for (TeleportTask task : pathGoals) task.execute();

        currentlyMoving = false;
        tickEventFuture.complete(null);
        return true;
    }


    public synchronized static void setPos(Vec3d pos, Boolean goBack, Boolean sendPackets, Boolean setClientSidedPos) {
        Packet.packetQueue.clear();
        ChatUtils.info("Starting Teleportation time: " + System.currentTimeMillis());
        returningToStart = goBack;
        setPos = setClientSidedPos;
        currentlyMoving = false;
        pathGoals.clear();
        to = pos;
        if (closeBy(currentPosition, to)) {
            ChatUtils.info("We are already close enough");
            return;
        }
        tickEventFuture = new CompletableFuture<>();
        pathGoals = Path.getPath(currentPosition, to);
        if (returningToStart) {
            returnPath = Path.getPath(to, currentPosition);
            int toRequired = Path.calculatePackets(pathGoals);
            int returnRequired = Path.calculatePackets(returnPath);
            if (toRequired > returnRequired) {
                bigestPath = pathGoals;
            } else {
                bigestPath = returnPath;
            }
        } else {
            bigestPath = pathGoals;
        }
        Boolean currentlyMoving = !attemptTeleport();

        if (currentlyMoving) {
            try {
                tickEventFuture.get();
            } catch (CompletionException | InterruptedException | ExecutionException e) {
                ChatUtils.error("ERROR");
            }
        }
        if (setPos) mc.execute(() -> mc.player.setPosition(to));
        if (sendPackets) Packet.sendAllPacketsInQueue();
    }
}
