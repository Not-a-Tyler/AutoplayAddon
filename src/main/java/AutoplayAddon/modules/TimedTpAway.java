package AutoplayAddon.modules;
import AutoplayAddon.AutoPlay.Movement.MoveToUtil;
import AutoplayAddon.AutoPlay.Movement.Movement;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.meteorclient.utils.player.PlayerUtils;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.orbit.EventPriority;
import net.minecraft.block.Blocks;
import AutoplayAddon.AutoPlay.Locator.CanPickUpTest;
import net.minecraft.entity.Entity;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.network.packet.c2s.play.TeleportConfirmC2SPacket;
import net.minecraft.network.packet.s2c.play.EntityPositionS2CPacket;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import net.minecraft.network.packet.s2c.play.CloseScreenS2CPacket;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import AutoplayAddon.AutoplayAddon;
import AutoplayAddon.AutoPlay.Locator.AirGapFinder;

import java.lang.reflect.Field;
import java.util.ArrayList;

import static meteordevelopment.meteorclient.MeteorClient.mc;

public class TimedTpAway extends Module {
    public TimedTpAway() {
        super(AutoplayAddon.autoplay, "timed-tp-away", "Teleports the player 100 units up after 30 ticks and then disables the module.");
    }

    private int tickCounter = 0;  // This will be our main counter

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        tickCounter++;  // Increment the counter on every tick

        // Check if 30 ticks have passed
        if (tickCounter >= 30) {
            Movement.currentPosition = mc.player.getPos();
            // Teleport the player 100 units up
            MoveToUtil.moveTo(new Vec3d(mc.player.getX(), mc.player.getY() + 100, mc.player.getZ()));
            ChatUtils.info("teleported");
            tickCounter = 0;
            // Disable the module
            toggle();
        }
    }
}
