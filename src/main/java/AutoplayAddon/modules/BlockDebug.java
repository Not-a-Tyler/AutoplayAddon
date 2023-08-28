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


public class BlockDebug extends Module {
    public BlockDebug() {
        super(AutoplayAddon.autoplay, "block-debug", "module used for testing");
    }
    @EventHandler(priority = EventPriority.LOWEST - 1)
    private void onSendPacket(PacketEvent.Send event) {

        if (event.packet instanceof TeleportConfirmC2SPacket) {
            TeleportConfirmC2SPacket packet = (TeleportConfirmC2SPacket) event.packet;
            ChatUtils.info("attempted to teleport with " + packet.getTeleportId());
            event.setCancelled(true);
            event.cancel();
        }

    }

}
