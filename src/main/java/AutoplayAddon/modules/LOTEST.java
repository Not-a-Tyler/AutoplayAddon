package AutoplayAddon.modules;

import AutoplayAddon.AutoPlay.Movement.Movement;
import AutoplayAddon.AutoPlay.Other.FastBox;
import AutoplayAddon.AutoplayAddon;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.settings.StringSetting;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.orbit.EventPriority;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.s2c.play.EntityAnimationS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityPositionS2CPacket;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket;
import net.minecraft.util.math.Vec3d;

public class LOTEST extends Module {

    public LOTEST() {
        super(AutoplayAddon.autoplay, "test-module-1", "bypass live overflows movement checks");
    }


    @EventHandler(priority = EventPriority.LOWEST - 1)
    private void onSendPacket(PacketEvent.Receive event) {
        if (event.packet instanceof EntitySpawnS2CPacket) {
            ChatUtils.info(" entity spawned " + ((EntitySpawnS2CPacket) event.packet).getId());
        }
        if (event.packet instanceof EntityVelocityUpdateS2CPacket) {
            ChatUtils.info(" entity velocity updated " + ((EntityVelocityUpdateS2CPacket) event.packet).getId());
        }
        if (event.packet instanceof EntityPositionS2CPacket) {
            ChatUtils.info(" entity position updated " + ((EntityPositionS2CPacket) event.packet).getId());
        }

//        if (event.packet instanceof EntityAnimationS2CPacket) {
//            ChatUtils.info(" entity animation " + ((EntityAnimationS2CPacket) event.packet).getId() + " animation id " + ((EntityAnimationS2CPacket) event.packet).getAnimationId());
//        }
    }



}
