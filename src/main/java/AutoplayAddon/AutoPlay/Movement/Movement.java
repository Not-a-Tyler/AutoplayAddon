package AutoplayAddon.AutoPlay.Movement;
import AutoplayAddon.AutoPlay.Other.FastBox;
import AutoplayAddon.Tracker.ServerSideValues;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.orbit.EventPriority;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static meteordevelopment.meteorclient.MeteorClient.mc;

public class Movement {
    public static ArrayList<FastBox> fastBoxList = new ArrayList<>();
    public static ArrayList<FastBox> fastBoxBadList = new ArrayList<>();
    public static boolean AutoSetPosition, autoSendPackets, rotationControl, AIDSboolean, currentlyMoving;
    public static Thread currentMovementThread;
    public static Vec3d currentPosition, to;
    public static float pitch, yaw;

    @EventHandler(priority = EventPriority.HIGHEST)
    private static void onServerPosUpdate(PacketEvent.Receive event) {
        if (event.packet instanceof PlayerPositionLookS2CPacket packet) {
            currentPosition = new Vec3d(packet.getX(), packet.getY(), packet.getZ());
            ChatUtils.error("Received packet and set current position to " + currentPosition);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST + 1)
    private static void onSendMovePacket(PacketEvent.Send event) {
        if (event.packet instanceof PlayerMoveC2SPacket) {
            event.setCancelled(true);
            event.cancel();
        }
    }

    public static boolean predictifPossible(Vec3d newPos, String reason) {
        int predict;
        double base = findFarthestDistance(newPos);
        int packetsRequired = ((int) Math.ceil(base / 10.0)) - 1;
        if (ServerSideValues.hasMoved) {
            predict = (packetsRequired * 2);
        } else {
            predict = packetsRequired;
        }
        double ivalue = predict + 1;
        double value = (predict - ServerSideValues.i2);
        if (value <= 0) value = 0;
        value = value + 1;

        //ChatUtils.info("For " + reason + " we will use " + ivalue + " packets so we need " + value);
        return (ServerSideValues.delta() >= value);
    }

    public static double findFarthestDistance(Vec3d newPos) {
        Vec3d tickpos = ServerSideValues.tickpos;
        List<Vec3d> positions = Arrays.asList(tickpos, currentPosition);
        return maxDist(newPos, positions);
    }

    public static double maxDist(Vec3d newPos, List<Vec3d> positions) {
        double maxDistance = Double.MIN_VALUE;
        for (Vec3d vec3d : positions) {
            double distance = calculateDistance(newPos, vec3d);
            if (distance > maxDistance) {
                maxDistance = distance;
            }
        }
        return maxDistance;
    }

    private static double calculateDistance(Vec3d pos1, Vec3d pos2) {
        double dx = pos2.x - pos1.x;
        double dy = pos2.y - pos1.y;
        double dz = pos2.z - pos1.z;
        return Math.sqrt(dx*dx + dy*dy + dz*dz);
    }



    public static void init(Boolean automaticallySetPosition, Boolean autopacket) {
        if (mc.player == null) return;
        MeteorClient.EVENT_BUS.unsubscribe(Movement.class);
        MeteorClient.EVENT_BUS.unsubscribe(GotoUtil.class);
        MeteorClient.EVENT_BUS.subscribe(GotoUtil.class);
        currentPosition = mc.player.getPos();
        mc.player.setNoGravity(true);
        to = mc.player.getPos();
        AIDSboolean = true;
        autoSendPackets = autopacket;
        AutoSetPosition = automaticallySetPosition;
    }
    public static void disable() {
        MeteorClient.EVENT_BUS.unsubscribe(GotoUtil.class);
        MeteorClient.EVENT_BUS.unsubscribe(Movement.class);
        AIDSboolean = false;
        GotoUtil.pathGoals.clear();
        currentlyMoving = false;
        if (mc.player == null) return;
        mc.player.setNoGravity(false);
    }

    public static boolean closeBy(Vec3d from, Vec3d to) {
        double dx = from.x - to.x;
        double dy = from.y - to.y;
        double dz = from.z - to.z;
        double squaredDistance = dx * dx + dy * dy + dz * dz;
        return squaredDistance < 0.005;
    }

    public static void moveTo(Vec3d pos) {
        if (currentMovementThread != null && currentMovementThread.isAlive()) {
            currentMovementThread.interrupt();
            ChatUtils.error("Interrupted previous movement thread");
        }
        boolean ignore;
        if (AIDSboolean) {
            ignore = false;
        } else {
            ignore = true;
        }
        new Thread(() -> {
            if (ignore) init(true, true);
            GotoUtil.setPos(pos, false);
            if (ignore) disable();
        }).start();

    }


}
