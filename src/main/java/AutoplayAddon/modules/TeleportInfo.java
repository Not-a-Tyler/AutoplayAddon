package AutoplayAddon.modules;

import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.orbit.EventPriority;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;
import AutoplayAddon.AutoplayAddon;

public class TeleportInfo extends Module {
    public TeleportInfo() {
        super(AutoplayAddon.autoplay, "upset-server-notify", "bypass anti human challenge");
    }

    @EventHandler(priority = EventPriority.HIGHEST + 1)
    private void onReceivePacket(PacketEvent.Receive event) {
        if (event.packet instanceof PlayerPositionLookS2CPacket packet) {
            info("Server Teleported you to: " +  packet.getX() + ", " + packet.getY() + ", " + packet.getZ());
        }
    }

}
