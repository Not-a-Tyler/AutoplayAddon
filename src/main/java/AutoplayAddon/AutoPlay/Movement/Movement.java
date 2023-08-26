package AutoplayAddon.AutoPlay.Movement;
import AutoplayAddon.Tracker.ServerSideValues;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.orbit.EventPriority;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;
import net.minecraft.util.math.Vec3d;
import static meteordevelopment.meteorclient.MeteorClient.mc;

public class Movement {
    public static Boolean AIDSboolean = false;
    public static double offset = 0;
    public static Thread currentMovementThread;
    public static Vec3d currentPosition, to;

    @EventHandler(priority = EventPriority.HIGHEST)
    private static void onServerPosUpdate(PacketEvent.Receive event) {
        if (event.packet instanceof PlayerPositionLookS2CPacket packet) {
            currentPosition = new Vec3d(packet.getX(), packet.getY(), packet.getZ());
            ChatUtils.info("Received packet and set current position to " + currentPosition);
            AIDS.disable();
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
        int packetsRequired = (int) Math.floor(Math.abs(base / 10.0));
        double delta = ServerSideValues.delta();
        if (!ServerSideValues.hasMoved) delta = 19;
        if (ServerSideValues.hasMoved) {
            predict = ((packetsRequired + 1) * 2);
        } else {
            predict = (packetsRequired + 2);
        }
        return !(delta < predict);
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


    public static void moveTo(Vec3d pos) {
        if (currentMovementThread != null && currentMovementThread.isAlive()) {
            currentMovementThread.interrupt();
            ChatUtils.info("Interrupted previous movement thread");
        }
        currentMovementThread = new Thread(() -> {
            if (AIDSboolean) {
                AIDS.setPos(pos);
                return;
            }
            to = pos;
            mc.player.setNoGravity(true);
            mc.player.setVelocity(Vec3d.ZERO);
            currentPosition = mc.player.getPos();
            MeteorClient.EVENT_BUS.subscribe(Movement.class);
            GotoUtil.shortGoTo();
            mc.player.setPosition(to);
            MeteorClient.EVENT_BUS.unsubscribe(Movement.class);
            mc.player.setNoGravity(false);
        });
        currentMovementThread.start();
    }


}
