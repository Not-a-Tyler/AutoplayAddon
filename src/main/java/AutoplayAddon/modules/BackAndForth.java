package AutoplayAddon.modules;

import AutoplayAddon.AutoPlay.Locator.CanPickUpTest;
import AutoplayAddon.AutoPlay.Movement.GotoUtil;
import AutoplayAddon.AutoPlay.Movement.Movement;
import AutoplayAddon.AutoPlay.Other.Packet;
import AutoplayAddon.AutoplayAddon;
import AutoplayAddon.Tracker.ServerSideValues;
import meteordevelopment.meteorclient.utils.misc.Keybind;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import net.minecraft.block.Blocks;
import net.minecraft.client.render.Camera;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.util.Collections;
import java.util.List;








import AutoplayAddon.AutoplayAddon;
import AutoplayAddon.Tracker.ServerSideValues;
import meteordevelopment.meteorclient.events.entity.player.StartBreakingBlockEvent;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.meteorclient.utils.player.SlotUtils;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.RaycastContext;


public class BackAndForth extends Module {

    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    public BackAndForth() {
        super(AutoplayAddon.autoplay, "BackAndForth", "Attempts to instantly mine blocks.");
    }
    private Vec3d pos1;
    private Vec3d pos2;
    private final Setting<Keybind> setpos1 = sgGeneral.add(new KeybindSetting.Builder()
        .name("setpos1")
        .description("Cancels sending packets and sends you back to your original position.")
        .defaultValue(Keybind.none())
        .action(() -> {
            Camera camera = mc.gameRenderer.getCamera();
            Vec3d cameraPos = camera.getPos();
            BlockPos pos = mc.world.raycast(new RaycastContext(cameraPos, cameraPos.add(Vec3d.fromPolar(camera.getPitch(), camera.getYaw()).multiply(197.0)), RaycastContext.ShapeType.OUTLINE, RaycastContext.FluidHandling.NONE, mc.player)).getBlockPos().up();
            pos1 = new Vec3d(pos.getX(), pos.getY(), pos.getZ());
        })
        .build()
    );

    private final Setting<Keybind> setpos2 = sgGeneral.add(new KeybindSetting.Builder()
        .name("setpos2")
        .description("Cancels sending packets and sends you back to your original position.")
        .defaultValue(Keybind.none())
        .action(() -> {
            Camera camera = mc.gameRenderer.getCamera();
            Vec3d cameraPos = camera.getPos();
            BlockPos pos = mc.world.raycast(new RaycastContext(cameraPos, cameraPos.add(Vec3d.fromPolar(camera.getPitch(), camera.getYaw()).multiply(197.0)), RaycastContext.ShapeType.OUTLINE, RaycastContext.FluidHandling.NONE, mc.player)).getBlockPos().up();
            pos2 = new Vec3d(pos.getX(), pos.getY(), pos.getZ());
        })
        .build()
    );



    @Override
    public void onActivate() {
        new Thread(() -> {
            while (true) {
                GotoUtil.setPos(pos1, false, true, false);
                GotoUtil.setPos(pos2, false, true, false);
            }
        }).start();
    }

}
