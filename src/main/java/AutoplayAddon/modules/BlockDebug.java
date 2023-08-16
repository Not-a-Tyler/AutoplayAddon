package AutoplayAddon.modules;
import AutoplayAddon.Tracker.BlockCache;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.meteorclient.utils.player.PlayerUtils;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.Blocks;
import AutoplayAddon.AutoPlay.Locator.CanPickUpTest;
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
import java.util.ArrayList;
import java.util.List;

public class BlockDebug extends Module {
    public BlockDebug() {
        super(AutoplayAddon.autoplay, "block-debug", "module used for testing");
    }


    @EventHandler
    private void onRender(Render3DEvent event) {
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
