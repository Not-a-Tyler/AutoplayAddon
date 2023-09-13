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

import static meteordevelopment.meteorclient.MeteorClient.mc;

public class Movement {
    public static ArrayList<FastBox> fastBoxList = new ArrayList<>();
    public static ArrayList<FastBox> fastBoxBadList = new ArrayList<>();

    public static Boolean AIDSboolean = false;
    public static Boolean rotationControl = false;
    public static Thread currentMovementThread;
    public static Vec3d currentPosition;
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
            event.cancel();
        }
    }

    public static boolean predictifPossible(Vec3d newPos) {
        int predict;
        double base = findFarthestDistance(newPos);
        int packetsRequired = ((int) Math.ceil(base / 10.0)) - 1;
        double delta = ServerSideValues.delta();
        if (ServerSideValues.hasMoved) {
            predict = (packetsRequired * 2) + 1;
        } else {
            predict = (packetsRequired + 1);
        }
        ChatUtils.info("Predicting " + predict + " packets");
        return (delta >= predict);
    }

    public static double findFarthestDistance(Vec3d newPos) {
        Vec3d tickpos = ServerSideValues.tickpos;
        double maxDistance = Double.MIN_VALUE;
        for (Vec3d vec3d : new Vec3d[] { tickpos, currentPosition }) {
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

    public static boolean closeBy(Vec3d from, Vec3d to) {
        double dx = from.x - to.x;
        double dy = from.y - to.y;
        double dz = from.z - to.z;
        double squaredDistance = dx * dx + dy * dy + dz * dz;
        return squaredDistance < 0.01;
    }

    public static void moveTo(Vec3d pos) {
        if (currentMovementThread != null && currentMovementThread.isAlive()) {
            currentMovementThread.interrupt();
            ChatUtils.error("Interrupted previous movement thread");
        }
        Boolean ignore;
        if (AIDSboolean) {
            ignore = false;
        } else {
            ignore = true;
        }
        new Thread(() -> {
            ChatUtils.info(System.currentTimeMillis() + " Starting movement");
            if (ignore) GotoUtil.init(false);
            GotoUtil.setPos(pos);
            if (ignore) GotoUtil.disable();
            ChatUtils.info(System.currentTimeMillis() + " Movement finished");
            mc.player.setPosition(pos);
        }).start();


    }


}
