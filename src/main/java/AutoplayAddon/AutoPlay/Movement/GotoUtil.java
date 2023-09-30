package AutoplayAddon.AutoPlay.Movement;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import AutoplayAddon.Mixins.ClientConnectionInvokerMixin;
import AutoplayAddon.Tracker.ServerSideValues;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.math.Vec3d;
import static meteordevelopment.meteorclient.MeteorClient.mc;

public class GotoUtil extends Movement {
    static Boolean extraPacket = false;
    static CompletableFuture<Integer> tickEventFuture;
    static Boolean returningToStart = false;
    static List<TeleportTask> bigestPath = new ArrayList<>();
    public static List<TeleportTask> returnPath = new ArrayList<>();
    static List<TeleportTask> pathGoals = new ArrayList<>();
    @EventHandler()
    private static void onPreTick(TickEvent.Pre event) {
        if (mc.player == null) return;
        if (AutoSetPosition && !closeBy(mc.player.getPos(), currentPosition)) {
            mc.player.setPos(currentPosition.x, currentPosition.y, currentPosition.z);
            //mc.player.setVelocity(Vec3d.ZERO);
        }
        if (!currentlyMoving) return;
        if (teleport()) {
            currentlyMoving = false;
            //ChatUtils.info("i2 before complete: " + ServerSideValues.i2);
            tickEventFuture.complete(ServerSideValues.i2);
        }
    }


    private static Boolean teleport() {
        //ChatUtils.info("delta is " + ServerSideValues.delta() + " time " + System.currentTimeMillis());
        int packetsRequired = Path.calculatePackets(bigestPath);

        int tempPackets = packetsRequired + pathGoals.size();
        int tempAllowed = ServerSideValues.predictallowedPlayerTicks();
        int tempI = ServerSideValues.i2;

        // we are going to do a basid simulatiom of the servers checks to see if we have allowedplayerticks high enough to teleport
        // we account for the packets required, we don't know if the player has moved
        if (returningToStart) {
            tempPackets += returnPath.size();
        }
        //ChatUtils.info("tempAllowed: " + tempAllowed + " tempI: " + ServerSideValues.i2 + " tempPackets: " + tempPackets);
        while (tempI < packetsRequired) {
            tempI++;

            if (tempI > Math.max(tempAllowed, 1)) {
                extraPacket = true;
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
            if (tempI > Math.max(tempAllowed, 1)) {
                extraPacket = true;
                return false;
            }
            // we know that the task has movement so we can subtract one
            tempAllowed -= 1;
        }

        while (packetsRequired > ServerSideValues.i2) {
            PlayerMoveC2SPacket packet;
            if (ServerSideValues.predictallowedPlayerTicks() > 20) {
                packet = new PlayerMoveC2SPacket.LookAndOnGround(yaw, pitch, true);
            } else {
                packet = new PlayerMoveC2SPacket.OnGroundOnly(true);
            }
            ServerSideValues.HandleMovePacketSafe(packet);
            ((ClientConnectionInvokerMixin) mc.getNetworkHandler().getConnection())._sendImmediately(packet, null);
        }
        for (TeleportTask task : pathGoals) task.execute();
        //ChatUtils.info("AFTER packets required: " + packetsRequired + " total packets: " + totalPackets + " i2: " + ServerSideValues.i2 + " time: " + System.currentTimeMillis());
        if (AutoSetPosition) {
            mc.player.setPos(to.x, to.y, to.z);
        }
        return true;
    }


    public static void setPos(Vec3d pos, Boolean goback) {
        returningToStart = goback;
        extraPacket = false;
        currentlyMoving = false;
        pathGoals.clear();
        to = pos;
        if (closeBy(currentPosition, to)) return;
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
//        int packetsRequired = Path.calculatePackets(pathGoals);
//        int totalPackets = packetsRequired + pathGoals.size();
//        ChatUtils.info("BEFORE packets required: " + packetsRequired + " total packets: " + totalPackets + " time: " + System.currentTimeMillis() + " i2 " + ServerSideValues.i2);
        if (teleport()) return;
        //ChatUtils.info("We need to wait");
        currentlyMoving = true;
        try {
            ServerSideValues.i2 = tickEventFuture.get();
            //ChatUtils.info("i2 after complete: " + ServerSideValues.i2);
        } catch (InterruptedException | ExecutionException e) {
            ChatUtils.error("ERROR: " + e);
        }
    }
}
