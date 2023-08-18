package AutoplayAddon.AutoPlay.Movement;
import AutoplayAddon.AutoplayAddon;
import AutoplayAddon.Mixins.PlayerMoveC2SPacketMixin;
import AutoplayAddon.Tracker.ServerSideValues;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.mixininterface.IPlayerMoveC2SPacket;
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
    private static void onReceivePacket(PacketEvent.Receive event) {
        if (event.packet instanceof PlayerPositionLookS2CPacket packet) {
            currentPosition = new Vec3d(packet.getX(), packet.getY(), packet.getZ());
            ChatUtils.info("Received packet and set current position to " + currentPosition);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST + 1)
    private static void onSendPacket(PacketEvent.Send event) {
        if (event.packet instanceof PlayerMoveC2SPacket) {
            IPlayerMoveC2SPacket iPacket = (IPlayerMoveC2SPacket) event.packet;
            if (iPacket.getTag() != 13377) {
                event.cancel();
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    private static void onSendPacketLowest(PacketEvent.Send event) {
        if (event.packet instanceof PlayerMoveC2SPacket.Full || event.packet instanceof PlayerMoveC2SPacket.PositionAndOnGround) {
            PlayerMoveC2SPacket packet = (PlayerMoveC2SPacket) event.packet;
            PlayerMoveC2SPacketMixin accessor = (PlayerMoveC2SPacketMixin) event.packet;
            accessor.setY(packet.getY(currentPosition.y) - offset);
        }
    }


    public static boolean predictifPossible(Vec3d newPos) {
        int predict;
        double base = findFarthestDistance(newPos);
        int packetsRequired = (int) Math.floor(Math.abs(base / 10.0));
        double delta = ServerSideValues.delta();
        if (!AutoplayAddon.values.hasMoved) delta = 19;
        if (AutoplayAddon.values.hasMoved) {
            predict = ((packetsRequired + 1) * 2);
            // ChatUtils.info("Predicted " + (delta - predict) + " since player has moved");
        } else {
            predict = (packetsRequired + 2);
            //ChatUtils.info("Predicted " + (delta - predict) + " since player has not moved");
        }
        if (delta < predict) {
            return false;
        } else {
            return true;
        }
    }

    public static double findFarthestDistance(Vec3d newPos) {
        Vec3d tickpos = AutoplayAddon.values.tickpos;
        double maxDistance = Double.MIN_VALUE;
        for (Vec3d vec3d : new Vec3d[] { tickpos, currentPosition }) {
            double distance = newPos.distanceTo(vec3d);
            if (distance > maxDistance) {
                maxDistance = distance;
            }
        }
        return maxDistance;
    }

    public static void moveTo(Vec3d pos) {
        if (currentMovementThread != null && currentMovementThread.isAlive()) {
            currentMovementThread.interrupt();
            //ChatUtils.info("Interrupted previous movement thread");
        }
        currentMovementThread = new Thread(() -> {
            if (AIDSboolean) {
               // ChatUtils.info("Started going to " + pos.toString() + "via AIDS");
                AIDS.setPos(pos);
                //ChatUtils.info("Finished Moving");
                return;
            }
            //ChatUtils.info("Started going to " + pos.toString() + "via normal method");
            to = pos;
            mc.player.setNoGravity(true);
            mc.player.setVelocity(Vec3d.ZERO);
            currentPosition = mc.player.getPos();
            MeteorClient.EVENT_BUS.subscribe(Movement.class);
            GotoUtil.shortGoTo();
            mc.player.setPosition(to);
            MeteorClient.EVENT_BUS.unsubscribe(Movement.class);
            mc.player.setNoGravity(false);
           // ChatUtils.info("Finished going to " + to.toString());
        });
        currentMovementThread.start();
    }


}
