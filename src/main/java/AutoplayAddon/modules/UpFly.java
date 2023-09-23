package AutoplayAddon.modules;


import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.misc.AutoReconnect;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.network.packet.c2s.play.VehicleMoveC2SPacket;
import net.minecraft.network.packet.s2c.play.DisconnectS2CPacket;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;
import AutoplayAddon.AutoplayAddon;


public class UpFly extends Module {
    public UpFly() {
        super(AutoplayAddon.autoplay, "up-fly", "bypass live overflows movement checks");
    }
    Vec3d currentPos;
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    public Setting<Integer> autoDiscHeight = sgGeneral.add(new IntSetting.Builder()
        .name("autoDiscHeight")
        .description("test")
        .defaultValue(20000000)
        .min(0)
        .sliderMax(20000000)
        .build()
    );

    private final Setting<Direction> mode = sgGeneral.add(new EnumSetting.Builder<Direction>()
        .name("direction")
        .description("The follow mode.")
        .defaultValue(Direction.Up)
        .build()
    );

    public enum Direction {
        Up,
        Down
    }

//    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    public Setting<Double> blocks = sgGeneral.add(new DoubleSetting.Builder()
            .name("Amount of logs to mine")
            .description("test")
            .defaultValue(199)
            .min(-200)
            .sliderMax(200)
            .build()
    );

//    @EventHandler(priority = EventPriority.HIGHEST)
//    private void onServerPosUpdate(PacketEvent.Receive event) {
//        if (event.packet instanceof PlayerPositionLookS2CPacket packet) {
//            currentPos = new Vec3d(packet.getX(), packet.getY(), packet.getZ());
//            ChatUtils.info("Received packet and set current position to " + currentPos);
//        }
//    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        mc.player.setNoGravity(true);

        if (mc.player.getY() < autoDiscHeight.get()) {
            Module autoReconnect = Modules.get().get(AutoReconnect.class);
            if (autoReconnect.isActive()) {
                autoReconnect.toggle();
            }
            // disconnect
            mc.player.networkHandler.onDisconnect(new DisconnectS2CPacket(Text.literal("too close to destination")));

            toggle();
            return;
        }

      //  if (currentPos == null) currentPos = mc.player.getPos();
        int packetsRequired = (int) Math.ceil(Math.abs(blocks.get() / 10)) - 1;
        //if (mode.get() == Direction.Up)
        if (mc.player.hasVehicle()) {
            for (int packetNumber = 0; packetNumber < (packetsRequired); packetNumber++) {
                mc.player.networkHandler.sendPacket(new VehicleMoveC2SPacket(mc.player.getVehicle()));
            }
            mc.player.getVehicle().setPos(mc.player.getVehicle().getX(), mc.player.getVehicle().getY() + blocks.get(), mc.player.getVehicle().getZ());
            mc.player.networkHandler.sendPacket(new VehicleMoveC2SPacket(mc.player.getVehicle()));
        } else {
            for (int packetNumber = 0; packetNumber < (packetsRequired); packetNumber++) {
                mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.OnGroundOnly(true));
            }
            mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(mc.player.getX(), mc.player.getY() + blocks.get(), mc.player.getZ(), true));
            mc.player.setPos(mc.player.getX(), mc.player.getY() + blocks.get(), mc.player.getZ());
        }
    }

}
