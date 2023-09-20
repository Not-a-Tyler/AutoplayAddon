package AutoplayAddon.AutoPlay.Movement;

import AutoplayAddon.Mixins.ClientConnectionInvokerMixin;
import AutoplayAddon.Tracker.ServerSideValues;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.math.Vec3d;
import java.util.Arrays;
import static meteordevelopment.meteorclient.MeteorClient.mc;



public class Teleport extends Movement {
    private Vec3d destination;
    private Vec3d currentPos;
    private int currentPackets;
    float yaw, pitch;
    public Teleport(Vec3d destination, Vec3d currentPos, int currentPackets) {
        this.currentPackets = currentPackets;
        this.yaw = rotationControl ? Movement.yaw : mc.player.getYaw();
        this.pitch = rotationControl ? Movement.pitch : mc.player.getPitch();
        this.currentPos = currentPos;
        this.destination = destination;
    }
    private void sendPacket(Vec3d position, Boolean setTo20) {
        PlayerMoveC2SPacket packet;
        if (setTo20) {
            packet = new PlayerMoveC2SPacket.OnGroundOnly(true);
        } else {
            packet = new PlayerMoveC2SPacket.Full(position.x, position.y, position.z, yaw, pitch, true);
        }
        ServerSideValues.HandleMovePacketSafe(packet);
        ((ClientConnectionInvokerMixin) mc.getNetworkHandler().getConnection())._sendImmediately(packet, null);
    }

    public int getPacketsRequired() {
        double base = maxDist(destination, Arrays.asList(ServerSideValues.tickpos, currentPos));
        return (((int) Math.ceil(base / 10.0)) - 1) - currentPackets;
    }

    public void execute(Boolean setTo20) {
//        double base = maxDist(destination, Arrays.asList(ServerSideValues.tickpos, currentPosition));
//        int packetsRequired = ((int) Math.ceil(base / 10.0)) - 1;
//        for (int i = 0; i < packetsRequired; i++) {
//            sendPacket(currentPosition);
//        }
        sendPacket(destination, setTo20);
        currentPosition = destination;
    }

}
