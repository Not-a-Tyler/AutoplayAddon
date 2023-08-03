package AutoplayAddon.modules;

import AutoplayAddon.AutoplayAddon;
import AutoplayAddon.Mixins.PlayerMoveC2SPacketMixin;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.orbit.EventPriority;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;

public class LOTEST extends Module {
    public LOTEST() {
        super(AutoplayAddon.autoplay, "live-overflow-test", "bypass live overflows movement checks");
    }
    @EventHandler(priority = EventPriority.LOWEST - 1)
    private void onSendPacket(PacketEvent.Send event) {
        if (event.packet instanceof PlayerMoveC2SPacket.PositionAndOnGround || event.packet instanceof PlayerMoveC2SPacket.Full) {
            PlayerMoveC2SPacketMixin accessor = (PlayerMoveC2SPacketMixin) event.packet;
            PlayerMoveC2SPacket packet = (PlayerMoveC2SPacket) event.packet;
            double x = Math.round(packet.getX(0) * 100.0) / 100.0; //round packets as best we can
            double z = Math.round(packet.getZ(0) * 100.0) / 100.0;

            x = Math.nextAfter(x, x + Math.signum(x));
            z = Math.nextAfter(z, z + Math.signum(z));
            accessor.setX(x);
            accessor.setZ(z);
        }
    }
}
