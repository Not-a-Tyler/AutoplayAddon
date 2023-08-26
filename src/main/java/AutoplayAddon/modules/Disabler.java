package AutoplayAddon.modules;
import AutoplayAddon.AutoplayAddon;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventPriority;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import meteordevelopment.orbit.EventHandler;

public class Disabler extends Module {
    public Disabler() {
        super(AutoplayAddon.autoplay, "disabler", "any anticheat disabler real dosent work XD");
    }


    @EventHandler(priority = EventPriority.HIGHEST + 3)
    private void onSendPacket(PacketEvent.Send event) {
        if (event.packet instanceof PlayerMoveC2SPacket.OnGroundOnly || event.packet instanceof PlayerMoveC2SPacket.PositionAndOnGround) {
            event.cancel();
            event.setCancelled(true);
            mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.Full(mc.player.getX(), mc.player.getY(), mc.player.getZ(), mc.player.getYaw(), mc.player.getPitch(), mc.player.isOnGround()));
        }
    }
}
