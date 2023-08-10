package AutoplayAddon.AutoPlay.Movement;
import meteordevelopment.meteorclient.mixininterface.IPlayerMoveC2SPacket;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import AutoplayAddon.AutoplayAddon;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.network.packet.c2s.play.VehicleMoveC2SPacket;
import net.minecraft.util.math.Vec3d;
import static meteordevelopment.meteorclient.MeteorClient.mc;
import java.text.DecimalFormat;


public class MoveToUtil {
    private static void sendpacket(PlayerMoveC2SPacket packet) {
        ((IPlayerMoveC2SPacket) packet).setTag(13377);
        mc.player.networkHandler.sendPacket(packet);
    }
    static DecimalFormat e = new DecimalFormat("#.##");;
    public static void moveTo(Vec3d newPos) {
        double base = MovementUtils.findFarthestDistance(newPos);
        int packetsRequired = (int) Math.floor(Math.abs(base / 10.0));
        sendpackets(packetsRequired);
        moveplayer(newPos);
    }

    public static void sendpackets(int packetsRequired) {
        //ChatUtils.info(" Started moving, sending " + packetsRequired + " packets");
        if (mc.player.hasVehicle()) {
            for (int packetNumber = 0; packetNumber < (packetsRequired); packetNumber++) {
                mc.player.networkHandler.sendPacket(new VehicleMoveC2SPacket(mc.player.getVehicle()));
            }
        } else {
            for (int packetNumber = 0; packetNumber < (packetsRequired); packetNumber++) {
                if (AutoplayAddon.values.allowedPlayerTicks > 20) {
                    sendpacket(new PlayerMoveC2SPacket.Full(mc.player.getX(), mc.player.getY(), mc.player.getZ(), mc.player.getYaw(), mc.player.getPitch(), true));
                } else {
                    sendpacket(new PlayerMoveC2SPacket.OnGroundOnly(true));
                }
            }
        }
    }
    public static void moveplayer(Vec3d newPos) {
        //ChatUtils.info("Finished moving to " + e.format(newPos.x) + ", " + e.format(newPos.y) + ", " + e.format(newPos.z));
        if (mc.player.hasVehicle()) {
            mc.player.getVehicle().setPosition(newPos.x, newPos.y, newPos.z);
            mc.player.networkHandler.sendPacket(new VehicleMoveC2SPacket(mc.player.getVehicle()));
        } else {
            if (AutoplayAddon.values.allowedPlayerTicks > 20) {
                sendpacket(new PlayerMoveC2SPacket.Full(newPos.x, newPos.y, newPos.z, mc.player.getYaw(), mc.player.getPitch(), true));
            } else {
                sendpacket(new PlayerMoveC2SPacket.PositionAndOnGround(newPos.x, newPos.y, newPos.z, true));
            }
            mc.player.setPosition(newPos.x, newPos.y, newPos.z);
        }
    }

}
