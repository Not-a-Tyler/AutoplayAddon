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
    private void onReceivePacket(PacketEvent.Receive event) {

        if (event.packet instanceof EntityPositionS2CPacket) {
            EntityPositionS2CPacket packet = ((EntityPositionS2CPacket) event.packet);
            ChatUtils.info("found via packet EntityPositionS2CPacket id: " + packet.getId());

        }

        if (event.packet instanceof  EntitySpawnS2CPacket) {

            EntitySpawnS2CPacket packet = ((EntitySpawnS2CPacket) event.packet);
            Vec3d pos = new Vec3d(packet.getX(), packet.getY(), packet.getZ());
            ChatUtils.info("found via packet EntitySpawnS2CPacket id: " + packet.getId() + " type " + packet.getEntityType().getName() + " position " + pos.toString());
            PlayerInteractEntityC2SPacket packet1 = PlayerInteractEntityC2SPacket.attack(mc.player, false);
            try {
                Field entityIdField = PlayerInteractEntityC2SPacket.class.getDeclaredField("entityId");
                entityIdField.setAccessible(true);
                entityIdField.set(packet1, packet.getId()); // Change this from 'packet' to 'packet1'
            } catch (NoSuchFieldException | IllegalAccessException e) {
                e.printStackTrace();
            }
            mc.player.networkHandler.sendPacket(packet1);

        }

    }

    @EventHandler
    private void onRender(Render3DEvent event) {
        for (Entity entity : mc.world.getEntities()) {
            if (entity != mc.player) {
                ChatUtils.info("found");
                mc.player.networkHandler.sendPacket(PlayerInteractEntityC2SPacket.attack(entity, mc.player.isSneaking()));
            }
        }

        if (AutoplayAddon.blockCache.lastBlockPos != null) {
            double x1 = AutoplayAddon.blockCache.lastBlockPos.getX();
            double y1 = AutoplayAddon.blockCache.lastBlockPos.getY();
            double z1 = AutoplayAddon.blockCache.lastBlockPos.getZ();
            double x2 = x1 + 1;
            double y2 = y1 + 1;
            double z2 = z1 + 1;

            event.renderer.box(x1, y1, z1, x2, y2, z2, new SettingColor(255, 0, 0, 255), new SettingColor(255, 0, 0, 255), ShapeMode.Both, 0);
        }
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        if (mc.player.isUsingItem()) return;

        if (mc.options.useKey.isPressed()) {
            HitResult hitResult = mc.player.raycast(5, 1f / 20f, false);

            if (hitResult.getType() == HitResult.Type.ENTITY && mc.player.interact(((EntityHitResult) hitResult).getEntity(), Hand.MAIN_HAND) != ActionResult.PASS) return;
            World world = mc.player.getEntityWorld();
            if (hitResult.getType() == HitResult.Type.BLOCK) {
                BlockPos pos = ((BlockHitResult) hitResult).getBlockPos();
                boolean hasAirBlockAboveOrBelow = world.getBlockState(pos.up()).getBlock() == Blocks.AIR || world.getBlockState(pos.down()).getBlock() == Blocks.AIR;
                boolean hasAirAdjacent = CanPickUpTest.hasAirAdjacent(pos);
                Vec3d airGapPos = AirGapFinder.findAirGapNearBlock(pos, 5);
                if (airGapPos != null) {
                    List<Vec3d> result = new ArrayList<>();
                    ChatUtils.info("AirGapPos: " + airGapPos);
                } else {
                    ChatUtils.info("AirGapPos: bad");
                }
                ChatUtils.info("hasAirAdjacent: " + hasAirAdjacent + " hasAirBlockAboveOrBelow: " + hasAirBlockAboveOrBelow + " Distance: " + PlayerUtils.distanceTo(pos));
            }
        }
    }

}
