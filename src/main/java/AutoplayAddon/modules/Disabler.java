package AutoplayAddon.modules;
import AutoplayAddon.AutoplayAddon;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.network.packet.c2s.play.VehicleMoveC2SPacket;
import meteordevelopment.orbit.EventHandler;

public class Disabler extends Module {
    public Disabler() {
        super(AutoplayAddon.autoplay, "move-disabler", "any anticheat disabler real dosent work XD");
    }
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Integer> amount = sgGeneral.add(new IntSetting.Builder()
        .name("disable packets per tick")
        .description("How much distance in each teleport?")
        .defaultValue(20)
        .min(0)
        .sliderMax(300)
        .build());


    @EventHandler
    private void onTick(TickEvent.Post event) {
        if (mc.player.hasVehicle()) {
            for (int i = 0; i < amount.get(); i++) {
                mc.player.networkHandler.sendPacket(new VehicleMoveC2SPacket(mc.player.getVehicle()));
            }
        } else {
            for (int i = 0; i < amount.get(); i++) {
                //mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.OnGroundOnly(false));
                //mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.LookAndOnGround(1, 1, true));
                mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.Full(mc.player.getX(), mc.player.getY(), mc.player.getZ(), 1, 1, true));
            }
        }
    }

}
