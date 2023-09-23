package AutoplayAddon.AutoPlay.Movement;
import AutoplayAddon.Mixins.ClientConnectionInvokerMixin;
import AutoplayAddon.Tracker.ServerSideValues;
import java.util.Queue;
import java.util.LinkedList;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.network.packet.c2s.play.VehicleMoveC2SPacket;
import net.minecraft.util.math.Vec3d;
import static meteordevelopment.meteorclient.MeteorClient.mc;


public class MoveToUtil extends Movement {
    public static Queue<Object> packetQueue = new LinkedList<>();

    public static void sendAllPacketsFromQueue() {
       // ChatUtils.info("SENDING " + packetQueue.size() + " packets");
        while (!packetQueue.isEmpty()) {
            Object packet = packetQueue.poll();
            if (packet instanceof PlayerMoveC2SPacket) {
                ((ClientConnectionInvokerMixin) mc.getNetworkHandler().getConnection())._sendImmediately((PlayerMoveC2SPacket) packet, null);
            } else if (packet instanceof VehicleMoveC2SPacket) {
                ((ClientConnectionInvokerMixin) mc.getNetworkHandler().getConnection())._sendImmediately((VehicleMoveC2SPacket) packet, null);
            }
        }
    }


    public static void handlePacket(PlayerMoveC2SPacket packet) {
        ServerSideValues.HandleMovePacketSafe(packet);
        packetQueue.add(packet);
    }

    private static float toYaw, toPitch;

    public static void moveTo(Vec3d newPos) {
        if (rotationControl) {
            toPitch = pitch;
            toYaw = yaw;
        } else {
            toPitch = mc.player.getPitch();
            toYaw = mc.player.getYaw();
        }
        double base = findFarthestDistance(newPos);
        int packetsRequired = ((int) Math.ceil(base / 10.0)) - 1;
      //  ChatUtils.info("Getting i value to " + packetsRequired);
        sendpackets(packetsRequired);
        moveplayer(newPos);
    }


    public static void sendpackets(int packetsRequired) {
        if (mc.player.hasVehicle()) {
            while ((packetsRequired > ServerSideValues.i2) && (ServerSideValues.delta() >= 0)) {
                packetQueue.add(new VehicleMoveC2SPacket(mc.player.getVehicle()));
            }
        } else {
            while ((packetsRequired > ServerSideValues.i2) && (ServerSideValues.delta() >= 0)) {
                //ChatUtils.info("sending charge packet");
                if (ServerSideValues.predictallowedPlayerTicks() > 20) {
                    handlePacket(new PlayerMoveC2SPacket.Full(currentPosition.x, currentPosition.y, currentPosition.z, toYaw, toPitch, true));
                } else {
                    handlePacket(new PlayerMoveC2SPacket.OnGroundOnly(true));
                }
            }
        }
    }

    public static void moveplayer(Vec3d newPos) {
        if (mc.player.hasVehicle()) {
            mc.player.getVehicle().setPosition(newPos.x, newPos.y, newPos.z);
            packetQueue.add(new VehicleMoveC2SPacket(mc.player.getVehicle()));
        } else {
          //  ChatUtils.info("sending tp packet");
            if (ServerSideValues.predictallowedPlayerTicks() > 20) {
                handlePacket(new PlayerMoveC2SPacket.Full(newPos.x, newPos.y, newPos.z, toYaw, toPitch, true));
            } else {
                handlePacket(new PlayerMoveC2SPacket.PositionAndOnGround(newPos.x, newPos.y, newPos.z, true));
            }
        }
        currentPosition = newPos;
    }
}
