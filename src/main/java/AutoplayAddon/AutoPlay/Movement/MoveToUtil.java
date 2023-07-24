package AutoplayAddon.AutoPlay.Movement;
import AutoplayAddon.AutoplayAddon;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.meteorclient.utils.player.PlayerUtils;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.network.packet.c2s.play.VehicleMoveC2SPacket;
import net.minecraft.util.math.Vec3d;
import static meteordevelopment.meteorclient.MeteorClient.mc;
import java.text.DecimalFormat;


public class MoveToUtil {

    public static void moveTo(Vec3d newPos) {
        int packetsRequired = (int) Math.ceil(PlayerUtils.distanceTo(newPos) / 10.0);
        DecimalFormat e = new DecimalFormat("#.##");
        ChatUtils.info("Moving to " + e.format(newPos.x) + ", " + e.format(newPos.y) + ", " + e.format(newPos.z));
        sendpackets(packetsRequired);
        moveplayer(newPos);
    }

    public static void sendpackets(int packetsRequired) {
        if (mc.player.hasVehicle()) {
            for (int packetNumber = 0; packetNumber < (packetsRequired); packetNumber++) {
                mc.player.networkHandler.sendPacket(new VehicleMoveC2SPacket(mc.player.getVehicle()));
            }
        } else {
            for (int packetNumber = 0; packetNumber < (packetsRequired); packetNumber++) {
                //if (AutoplayAddon.values.allowedPlayerTicks < 20) {
                //    mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.LookAndOnGround(mc.player.getYaw(), mc.player.getPitch(), true));
                //} else {
                mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.OnGroundOnly(true));
                //}
            }
        }
    }
    public static void moveplayer(Vec3d newPos) {
        if (mc.player.hasVehicle()) {
            mc.player.getVehicle().setPosition(newPos.x, newPos.y, newPos.z);
            mc.player.networkHandler.sendPacket(new VehicleMoveC2SPacket(mc.player.getVehicle()));
        } else {
            //if (AutoplayAddon.values.allowedPlayerTicks < 20) {
            //    mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.Full(newPos.x, newPos.y, newPos.z, mc.player.getYaw(), mc.player.getPitch(), true));
            //} else {
            mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(newPos.x, newPos.y, newPos.z, true));
            //}
            mc.player.setPosition(newPos.x, newPos.y, newPos.z);
        }
    }


}
