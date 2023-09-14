package AutoplayAddon.AutoPlay.Movement;

import AutoplayAddon.Mixins.ClientConnectionInvokerMixin;
import AutoplayAddon.Tracker.ServerSideValues;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.math.Vec3d;

import static meteordevelopment.meteorclient.MeteorClient.mc;

interface Task {

    void execute();

    boolean isDone();
}

class WaitTask extends Movement implements Task {
    private int remainingTicks;

    public WaitTask(int ticks) {
        this.remainingTicks = ticks;
    }

    @Override
    public void execute() {
        if (remainingTicks > 0) remainingTicks--;
    }

    @Override
    public boolean isDone() {
        return remainingTicks <= 0;
    }
}

abstract class PacketTask extends Movement implements Task {
    protected boolean setTo20;

    protected void sendPacket(Vec3d position) {
        PlayerMoveC2SPacket packet;
        float yaw = rotationControl ? Movement.yaw : mc.player.getYaw();
        float pitch = rotationControl ? Movement.pitch : mc.player.getPitch();
        if (setTo20) {
             packet = new PlayerMoveC2SPacket.OnGroundOnly(true);
        } else {
            packet = new PlayerMoveC2SPacket.Full(position.x, position.y, position.z, yaw, pitch, true);
        }
        ServerSideValues.HandleMovePacketSafe(packet);
        ((ClientConnectionInvokerMixin) mc.getNetworkHandler().getConnection())._sendImmediately(packet, null);
    }
}

class TeleportTo extends PacketTask {
    private Vec3d destination;

    public TeleportTo(Vec3d destination, boolean setTo20) {
        this.destination = destination;
        this.setTo20 = setTo20;
    }

    @Override
    public void execute() {
        sendPacket(destination);
    }

    @Override
    public boolean isDone() {
        return true;
    }
}

class SendPackets extends PacketTask {
    private int packetCount;

    public SendPackets(int packetCount, boolean setTo20) {
        this.packetCount = packetCount;
        this.setTo20 = setTo20;
    }

    @Override
    public void execute() {
        for (int i = 0; i < packetCount; i++) {
            sendPacket(currentPosition);
        }
    }

    @Override
    public boolean isDone() {
        return true;
    }
}
