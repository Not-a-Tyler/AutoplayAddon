package AutoplayAddon.modules;
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
import java.util.List;

import static meteordevelopment.meteorclient.MeteorClient.mc;

public class AntiKick extends Module {
    public AntiKick() {
        super(AutoplayAddon.autoplay, "anti-afk-test", "Example");
    }

    private int tickCounter = 0;  // This will be our main counter
    private boolean shiftPressed = false; // To keep track of current shift state

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        tickCounter++;  // Increment the counter on every tick

        // Send START_FALL_FLYING every 2 ticks
        if (tickCounter % 2 == 0) {
            mc.player.networkHandler.sendPacket(new ClientCommandC2SPacket(mc.player, ClientCommandC2SPacket.Mode.STOP_SLEEPING));
        }

        // Alternate between RELEASE_SHIFT_KEY and PRESS_SHIFT_KEY every 20 ticks
        if (tickCounter % 20 == 0) {
            if (shiftPressed) {
                mc.player.networkHandler.sendPacket(new ClientCommandC2SPacket(mc.player, ClientCommandC2SPacket.Mode.RELEASE_SHIFT_KEY));
                shiftPressed = false;
            } else {
                mc.player.networkHandler.sendPacket(new ClientCommandC2SPacket(mc.player, ClientCommandC2SPacket.Mode.PRESS_SHIFT_KEY));
                shiftPressed = true;
            }
        }
    }

}
