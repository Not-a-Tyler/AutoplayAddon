package AutoplayAddon.modules;
import AutoplayAddon.AutoplayAddon;
import AutoplayAddon.Mixins.PlayerMoveC2SPacketMixin;
import AutoplayAddon.Tracker.ServerSideValues;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.mixininterface.IPlayerMoveC2SPacket;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.orbit.EventPriority;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.util.math.Vec3d;
import org.w3c.dom.events.Event;

public class Disabler extends Module {
    public Disabler() {
        super(AutoplayAddon.autoplay, "disabler", "any anticheat disabler real dosent work XD");
    }
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final Setting<Boolean> extraStrict = sgGeneral.add(new BoolSetting.Builder()
        .name("extra-strict")
        .description("Renders a block overlay where you will be teleported.")
        .defaultValue(true)
        .build()
    );

    @EventHandler(priority = EventPriority.HIGHEST + 2)
    private void onSendPacket(PacketEvent.Send event) {
        if (event.packet instanceof PlayerMoveC2SPacket) {
            PlayerMoveC2SPacket packet = (PlayerMoveC2SPacket) event.packet;
            IPlayerMoveC2SPacket iPacket = ((IPlayerMoveC2SPacket) event.packet);
            if (iPacket.getTag() == 13377) return;
            double X = packet.getX(mc.player.getX());
            double Y = packet.getY(mc.player.getY());
            double Z = packet.getZ(mc.player.getZ());
            float Yaw = packet.getYaw(mc.player.getYaw());
            float Pitch = packet.getPitch(mc.player.getPitch());
            boolean Ground = packet.isOnGround();
            PlayerMoveC2SPacket newPacket = null;
            if (ServerSideValues.predictallowedPlayerTicks() > 20) {
                if (packet.changesLook()) return;
                newPacket = new PlayerMoveC2SPacket.Full(X, Y, Z, Yaw, Pitch, Ground);
            } else {
                if (extraStrict.get()) {
                    if (packet instanceof PlayerMoveC2SPacket.LookAndOnGround) {
                        event.setCancelled(true);
                        event.cancel();
                        return;
                    }
                    if (packet instanceof PlayerMoveC2SPacket.Full) {
                        newPacket = new PlayerMoveC2SPacket.PositionAndOnGround(X, Y, Z, Ground);
                    }
                } else {
                    return;
                }
            }
            if (newPacket == null) return;
            ((IPlayerMoveC2SPacket) newPacket).setTag(13377);
            mc.player.networkHandler.sendPacket(newPacket);
            event.setCancelled(true);
            event.cancel();
        }
    }
}

