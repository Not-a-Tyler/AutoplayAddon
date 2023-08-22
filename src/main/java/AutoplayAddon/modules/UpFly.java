package AutoplayAddon.modules;

import AutoplayAddon.AutoPlay.Controller.SmartMine;
import AutoplayAddon.AutoPlay.Inventory.Lists;
import AutoplayAddon.AutoPlay.Movement.CanTeleport;
import AutoplayAddon.AutoplayAddon;
import AutoplayAddon.Mixins.ClientConnectionInvokerMixin;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.mixininterface.IPlayerMoveC2SPacket;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.orbit.EventHandler;
import AutoplayAddon.AutoplayAddon;



import AutoplayAddon.AutoPlay.Movement.AIDS;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.orbit.EventPriority;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.network.packet.c2s.play.VehicleMoveC2SPacket;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;
import net.minecraft.util.math.Vec3d;
import AutoplayAddon.AutoPlay.Movement.GotoUtil;
import AutoplayAddon.AutoplayAddon;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static meteordevelopment.meteorclient.MeteorClient.mc;


public class UpFly extends Module {
    public UpFly() {
        super(AutoplayAddon.autoplay, "up-fly", "bypass live overflows movement checks");
    }
    Vec3d currentPos;
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    public Setting<Integer> blocks = sgGeneral.add(new IntSetting.Builder()
            .name("Amount of logs to mine")
            .description("test")
            .defaultValue(3)
            .min(0)
            .sliderMax(100)
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
      //  if (currentPos == null) currentPos = mc.player.getPos();
        int packetsRequired = (int) Math.ceil(Math.abs(blocks.get() / 10));

        if (mc.player.hasVehicle()) {
            for (int packetNumber = 0; packetNumber < (packetsRequired - 1); packetNumber++) {
                mc.player.networkHandler.sendPacket(new VehicleMoveC2SPacket(mc.player.getVehicle()));
            }
            mc.player.getVehicle().setPosition(mc.player.getVehicle().getX(), mc.player.getVehicle().getY() + blocks.get(), mc.player.getVehicle().getZ());
            mc.player.networkHandler.sendPacket(new VehicleMoveC2SPacket(mc.player.getVehicle()));
        } else {
            for (int packetNumber = 0; packetNumber < (packetsRequired - 1); packetNumber++) {
                mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.OnGroundOnly(true));
            }
            mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(mc.player.getX(), mc.player.getY() + blocks.get(), mc.player.getZ(), true));
            mc.player.setPosition(mc.player.getX(), mc.player.getY() + blocks.get(), mc.player.getZ());
        }
    }

}
