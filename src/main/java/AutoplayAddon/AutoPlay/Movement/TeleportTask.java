package AutoplayAddon.AutoPlay.Movement;

import AutoplayAddon.Mixins.ClientConnectionInvokerMixin;
import AutoplayAddon.Tracker.ServerSideValues;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.math.Vec3d;
import java.util.Arrays;
import static meteordevelopment.meteorclient.MeteorClient.mc;



public class TeleportTask extends Movement {
    private Vec3d destination;
    private Vec3d currentPos;
    private int currentPackets;
    public TeleportTask(Vec3d destination, Vec3d currentPos, int currentPackets) {
        this.currentPackets = currentPackets;
        this.currentPos = currentPos;
        this.destination = destination;
    }
    private void sendPacket(Vec3d position) {
        PlayerMoveC2SPacket packet;
        if (ServerSideValues.predictallowedPlayerTicks() > 20) {
            packet = new PlayerMoveC2SPacket.Full(position.x, position.y, position.z, mc.player.getYaw(), mc.player.getPitch(), true);
        } else {
            packet = new PlayerMoveC2SPacket.PositionAndOnGround(position.x, position.y, position.z, true);
        }
        ServerSideValues.HandleMovePacketSafe(packet);
        ((ClientConnectionInvokerMixin) mc.getNetworkHandler().getConnection())._sendImmediately(packet, null);
    }

    public int getPacketsRequired() {
        // server will get the amount of packets required from either the last tick position or the players position
        // which ever is larger
        double base = maxDist(destination, Arrays.asList(ServerSideValues.tickpos, currentPos));
        //ChatUtils.info("distance for pathfind tp is " + base);
        return (((int) Math.ceil(base / 10.0)) - 1) - currentPackets;
    }

    public void execute() {
//        double base = maxDist(destination, Arrays.asList(ServerSideValues.tickpos, currentPosition));
//        int packetsRequired = ((int) Math.ceil(base / 10.0)) - 1;
//        for (int i = 0; i < packetsRequired; i++) {
//            sendPacket(currentPosition);
//        }
        sendPacket(destination);
        currentPosition = destination;
    }

}
