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



public class GotoQueue extends Movement {
    static CompletableFuture<Void> tickEventFuture;
    static double y;
    static int totalPackets, stage, packetsRequired;
    public static List<Teleport> tasks = new ArrayList<>();
    @EventHandler()
    private static void onPreTick(TickEvent.Pre event) {
        if (mc.player == null) return;
        if (AutoSetPosition && !closeBy(mc.player.getPos(), currentPosition)) {
            mc.player.setPos(to.x, to.y, to.z);
            mc.player.setVelocity(Vec3d.ZERO);
        }
        if (!currentlyMoving) return;
       // ChatUtils.info("delta is " + ServerSideValues.delta());
        if (teleport()) {
          //  ChatUtils.info("we teleported");
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
        // predict if the x teleports is possible due to allowedplayerticks and i value on server
        // after we move the allowedplayer ticks is gonna subtract one
        // we know that we will move after the teleport
        int tempPackets;
        calculatePackets();
        if (ServerSideValues.hasMoved) {
            tempPackets = totalPackets * 2;
        } else {
            tempPackets = totalPackets + (tasks.size() * 2);
        }
        if (ServerSideValues.delta() >= tempPackets){
            int packetCount = 0;  // Create a packet counter
            int packetLimit = 500;  // Define the packet limit

            while (packetsRequired > ServerSideValues.i2 && packetCount < packetLimit) { // Added check for packetCount here
                PlayerMoveC2SPacket packet;
                if (ServerSideValues.predictallowedPlayerTicks() > 20) {
                    packet = new PlayerMoveC2SPacket.LookAndOnGround(yaw, pitch, true);
                } else {
                    packet = new PlayerMoveC2SPacket.OnGroundOnly(true);
                }
                ServerSideValues.HandleMovePacketSafe(packet);
                ((ClientConnectionInvokerMixin) mc.getNetworkHandler().getConnection())._sendImmediately(packet, null);

                packetCount++;  // Increment the packet counter after sending a packet
            }

            for (Teleport task : tasks) task.execute();
            //ChatUtils.info("AFTER packets required: " + packetsRequired + " total packets: " + packetCount);
            if (AutoSetPosition) mc.player.setPos(to.x, to.y, to.z);
            return true;
        } else {
            return false;
        }
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
        y = -13377;
        stage = 0;
        totalPackets = 0;
        tickEventFuture = new CompletableFuture<>();
        Vec3d currentTempPos = currentPosition;
        while (true) {
            if (stage == 4) {
                ChatUtils.error("stage is 4");
                break;
            }
            if (CanTeleport.lazyCheck(currentTempPos, to)) {
                tasks.add(new Teleport(to, currentTempPos, tasks.size()));
                break;
            } else if (y == -13377) {
                y = CanTeleport.searchY(currentPosition, to);
                if (y == -1337) {
                    y = currentPosition.y;
                    stage = 2;
                } else {
                    stage = 1;
                }
            }
            Vec3d newPos = getStage(currentTempPos, to, stage);
            tasks.add(new Teleport(newPos, currentTempPos, tasks.size()));
            currentTempPos = newPos;
            stage++;
        }
        calculatePackets();
        //ChatUtils.info("BEFORE packets required: " + packetsRequired + " total packets: " + totalPackets);
        if (teleport()) return;
        //ChatUtils.info("allowedplayerticks is too low");
        currentlyMoving = true;
        try {
            tickEventFuture.get();
        } catch (InterruptedException | ExecutionException e) {
            ChatUtils.error("goto utils interupped " + e);
        }
    }
}
