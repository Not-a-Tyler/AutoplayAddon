package AutoplayAddon.Tracker;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.orbit.EventPriority;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
public class ServerSideValues {
    double prevx = 0;
    double prevy = 0;
    double prevz = 0;
    public int i = 0;
    public int lasttick = 0;
    private int receivedMovePacketCount;
    private int knownMovePacketCount;


    @EventHandler
    private void onTick(TickEvent.Post event) {

        knownMovePacketCount = receivedMovePacketCount;
    }

    public static int allowedPlayerTicks = 20;


    @EventHandler(priority = EventPriority.LOWEST - 3)
    private void onSendPacket(PacketEvent.Send event) {
        System.out.println("send packet");
        if (event.packet instanceof PlayerMoveC2SPacket) {
            PlayerMoveC2SPacket packet = (PlayerMoveC2SPacket) event.packet;
            double d10 = 0;
            if (event.packet instanceof PlayerMoveC2SPacket.Full || event.packet instanceof PlayerMoveC2SPacket.PositionAndOnGround) {
                double d0 = packet.getX(prevx);
                double d1 = packet.getY(prevy);
                double d2 = packet.getZ(prevz);


                double currDeltaX = d0 - prevx;
                double currDeltaY = d1 - prevy;
                double currDeltaZ = d2 - prevz;

                d10 = (currDeltaX * currDeltaX + currDeltaY * currDeltaY + currDeltaZ * currDeltaZ);
            }

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


            //ChatUtils.info("allowed: " + allowedPlayerTicks + " i: " + i);
            if (event.packet instanceof PlayerMoveC2SPacket.Full || event.packet instanceof PlayerMoveC2SPacket.PositionAndOnGround) {
                prevx = packet.getX(prevx);
                prevy = packet.getY(prevy);
                prevz = packet.getZ(prevz);
            }
        }
    }
}
