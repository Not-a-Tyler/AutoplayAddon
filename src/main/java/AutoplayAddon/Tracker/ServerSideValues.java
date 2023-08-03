package AutoplayAddon.Tracker;
import AutoplayAddon.AutoplayAddon;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.orbit.EventPriority;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.math.Vec3d;

import static meteordevelopment.meteorclient.MeteorClient.mc;

public class ServerSideValues {
    public boolean hasMoved = false;
    double prevx, prevy, prevz= 0;
    public Vec3d tickpos = new Vec3d(0,0,0);
    public int i, allowedPlayerTicks = 0;
    private int receivedMovePacketCount, knownMovePacketCount, lasttick = 0;

    public void init() {
        MeteorClient.EVENT_BUS.subscribe(this);
    }

    public static int delta() {
        return AutoplayAddon.values.allowedPlayerTicks - AutoplayAddon.values.i;
    }


    @EventHandler(priority = EventPriority.LOWEST)
    private void onTick(TickEvent.Pre event) {
        hasMoved = false;
        if (mc.player == null) return;
        tickpos = mc.player.getPos();
        knownMovePacketCount = receivedMovePacketCount;
    }



    @EventHandler(priority = EventPriority.LOWEST - 2)
    private void onSendPacket(PacketEvent.Send event) {
        if (event.packet instanceof PlayerMoveC2SPacket) {
            PlayerMoveC2SPacket packet = (PlayerMoveC2SPacket) event.packet;
            double d10 = 0;
            double d0 = packet.getX(prevx);
            double d1 = packet.getY(prevy);
            double d2 = packet.getZ(prevz);
            boolean hasPos = (event.packet instanceof PlayerMoveC2SPacket.Full || event.packet instanceof PlayerMoveC2SPacket.PositionAndOnGround);
            boolean hasRot = (event.packet instanceof PlayerMoveC2SPacket.LookAndOnGround || event.packet instanceof PlayerMoveC2SPacket.Full);

            if (hasPos) {
                double currDeltaX = d0 - prevx;
                double currDeltaY = d1 - prevy;
                double currDeltaZ = d2 - prevz;
                d10 = (currDeltaX * currDeltaX + currDeltaY * currDeltaY + currDeltaZ * currDeltaZ);
            }

            double d6 = d0 - tickpos.x;
            double d7 = d1 - tickpos.y;
            double d8 = d2 - tickpos.z;

            d10 = Math.max((d6 * d6 + d7 * d7 + d8 * d8), d10);
            if (d10 > 0) {
                hasMoved = true;
            }

            ++receivedMovePacketCount;
            i = receivedMovePacketCount - knownMovePacketCount;
            allowedPlayerTicks += (System.currentTimeMillis() / 50) - lasttick;
            allowedPlayerTicks = Math.max(allowedPlayerTicks, 1);
            lasttick = (int) (System.currentTimeMillis() / 50);
            if (i > Math.max(allowedPlayerTicks, 5)) {
                i = 1;
            }

            if (hasRot || d10 > 0) {
                allowedPlayerTicks -= 1;
            } else {
                allowedPlayerTicks = 20;
            }
            if (d10 > 0) {
                ChatUtils.info("allowed: " + allowedPlayerTicks + " i: " + i + " delta: " + delta() + " MOVED D10: " + d10);
            } else {
                ChatUtils.info("allowed: " + allowedPlayerTicks + " i: " + i + " delta: " + delta());
            }
            if (hasPos) {
                prevx = packet.getX(0);
                prevy = packet.getY(0);
                prevz = packet.getZ(0);
            }
        }
    }

}
