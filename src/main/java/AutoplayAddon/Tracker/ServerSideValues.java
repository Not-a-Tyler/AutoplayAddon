package AutoplayAddon.Tracker;
import AutoplayAddon.AutoplayAddon;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.orbit.EventPriority;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;

import static meteordevelopment.meteorclient.MeteorClient.mc;

public class ServerSideValues {
    public boolean moveable = true;
    double prevx, prevy, prevz, tickx, ticky, tickz = 0;
    public int i, allowedPlayerTicks = 0;
    private int receivedMovePacketCount, knownMovePacketCount, lasttick = 0;

    public void init() {
        MeteorClient.EVENT_BUS.subscribe(this);
    }

    public static int delta() {
        return AutoplayAddon.values.allowedPlayerTicks - AutoplayAddon.values.i;
    }
    @EventHandler(priority = EventPriority.HIGHEST + 3)
    private void onTick(TickEvent.Post event) {
        moveable = true;
        if (mc.player == null) return;
        tickx = mc.player.getX();
        ticky = mc.player.getY();
        tickz = mc.player.getZ();
        knownMovePacketCount = receivedMovePacketCount;
    }

    @EventHandler(priority = EventPriority.LOWEST - 3)
    private void onSendPacket(PacketEvent.Send event) {
        if (event.packet instanceof PlayerMoveC2SPacket) {
            PlayerMoveC2SPacket packet = (PlayerMoveC2SPacket) event.packet;
            double d10 = 0;
            double d0 = packet.getX(prevx);
            double d1 = packet.getY(prevy);
            double d2 = packet.getZ(prevz);
            if (event.packet instanceof PlayerMoveC2SPacket.Full || event.packet instanceof PlayerMoveC2SPacket.PositionAndOnGround) {
                moveable = false;
                double currDeltaX = d0 - prevx;
                double currDeltaY = d1 - prevy;
                double currDeltaZ = d2 - prevz;
                d10 = (currDeltaX * currDeltaX + currDeltaY * currDeltaY + currDeltaZ * currDeltaZ);
            }

            double d6 = d0 - tickx;
            double d7 = d1 - ticky;
            double d8 = d2 - tickz;

            d10 = Math.max((d6 * d6 + d7 * d7 + d8 * d8), d10);

            ++receivedMovePacketCount;
            i = receivedMovePacketCount - knownMovePacketCount;
            allowedPlayerTicks += (System.currentTimeMillis() / 50) - lasttick;
            allowedPlayerTicks = Math.max(allowedPlayerTicks, 1);
            lasttick = (int) (System.currentTimeMillis() / 50);
            if (i > Math.max(allowedPlayerTicks, 5)) {
                i = 1;
            }

            if (packet.changesLook() || d10 > 0) {
                allowedPlayerTicks -= 1;
            } else {
                allowedPlayerTicks = 20;
            }

            //ChatUtils.info("allowed: " + allowedPlayerTicks + " i: " + i + " delta: " + delta());
            if (event.packet instanceof PlayerMoveC2SPacket.Full || event.packet instanceof PlayerMoveC2SPacket.PositionAndOnGround) {
                prevx = packet.getX(prevx);
                prevy = packet.getY(prevy);
                prevz = packet.getZ(prevz);
            }
        }
    }

}
