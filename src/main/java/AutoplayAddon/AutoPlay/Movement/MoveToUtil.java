package AutoplayAddon.AutoPlay.Movement;
import AutoplayAddon.Mixins.ClientConnectionInvokerMixin;
import AutoplayAddon.Tracker.ServerSideValues;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.movement.Flight;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.network.packet.c2s.play.VehicleMoveC2SPacket;
import net.minecraft.util.math.Vec3d;
import static meteordevelopment.meteorclient.MeteorClient.mc;
import java.text.DecimalFormat;


public class MoveToUtil extends Movement {
    private static void sendpacket(PlayerMoveC2SPacket packet) {
        ((ClientConnectionInvokerMixin) mc.getNetworkHandler().getConnection())._sendImmediately(packet, null);
        ServerSideValues.HandleMovepacket(packet);
    }

    private static float toYaw, toPitch;
    static DecimalFormat e = new DecimalFormat("#.##");

    public static void moveTo(Vec3d newPos) {
        //ChatUtils.info("starting move to");
        Module flight = Modules.get().get(Flight.class);
        if (flight.isActive()) {
            flight.toggle();
        }

        if (rotationControl) {
            toPitch = pitch;
            toYaw = yaw;
        } else {
            toPitch = mc.player.getPitch();
            toYaw = mc.player.getYaw();
        }
        double base = findFarthestDistance(newPos);
        int packetsRequired = (int) Math.floor(Math.abs(base / 10.0));
        sendpackets(packetsRequired);
        moveplayer(newPos);
        //ChatUtils.info("ending move to");
    }


    public static void sendpackets(int packetsRequired) {
        if (mc.player.hasVehicle()) {
            for (int packetNumber = 0; packetNumber < (packetsRequired); packetNumber++) {
                mc.player.networkHandler.sendPacket(new VehicleMoveC2SPacket(mc.player.getVehicle()));
            }
        } else {
            for (int packetNumber = 0; packetNumber < (packetsRequired); packetNumber++) {
                if (ServerSideValues.allowedPlayerTicks > 20) {
                    sendpacket(new PlayerMoveC2SPacket.Full(currentPosition.x, currentPosition.y, currentPosition.z, toYaw, toPitch, true));
                } else {
                    sendpacket(new PlayerMoveC2SPacket.OnGroundOnly(true));
                }
            }
        }
    }

    public static void moveplayer(Vec3d newPos) {
        if (mc.player.hasVehicle()) {
            mc.player.getVehicle().setPosition(newPos.x, newPos.y, newPos.z);
            mc.player.networkHandler.sendPacket(new VehicleMoveC2SPacket(mc.player.getVehicle()));
        } else {
            if (ServerSideValues.allowedPlayerTicks > 20) {
                sendpacket(new PlayerMoveC2SPacket.Full(newPos.x, newPos.y, newPos.z, toYaw, toPitch, true));
            } else {
                sendpacket(new PlayerMoveC2SPacket.PositionAndOnGround(newPos.x, newPos.y, newPos.z, true));
            }
        }
        currentPosition = newPos;
    }
}
