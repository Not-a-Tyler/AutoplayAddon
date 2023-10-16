package AutoplayAddon.modules;

import AutoplayAddon.AutoplayAddon;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.*;
import net.minecraft.util.math.BlockPos;

public class PacketLogger extends Module {
    public PacketLogger() {
        super(AutoplayAddon.autoplay, "packet-logger", "Attempts to instantly mine blocks.");
    }



    @EventHandler
    private void onSendPacket(PacketEvent.Send event) {
        ChatUtils.info("Sent packet " + event.packet.getClass().getSimpleName());
    }
}
