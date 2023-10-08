package AutoplayAddon.modules;
import AutoplayAddon.AutoPlay.Movement.Movement;
import AutoplayAddon.AutoplayAddon;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.misc.Keybind;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.render.Camera;
import net.minecraft.entity.EntityPose;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;


public class ClickTp extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgRender = settings.createGroup("Render");
    public static Thread currentMovementThread;


    private final Setting<Boolean> render = sgRender.add(new BoolSetting.Builder()
        .name("render")
        .description("Renders a block overlay where you will be teleported.")
        .defaultValue(true)
        .build()
    );
    private final Setting<ShapeMode> shapeMode = sgRender.add(new EnumSetting.Builder<ShapeMode>()
        .name("shape-mode")
        .description("How the shapes are rendered.")
        .defaultValue(ShapeMode.Both)
        .visible(() -> render.get())
        .build()
    );

    private final Setting<SettingColor> sideColor = sgRender.add(new ColorSetting.Builder()
        .name("side-color-solid-block")
        .description("The color of the sides of the blocks being rendered.")
        .defaultValue(new SettingColor(255, 0, 255, 15))
        .visible(() -> render.get())
        .build()
    );

    private final Setting<SettingColor> lineColor = sgRender.add(new ColorSetting.Builder()
        .name("line-color-solid-block")
        .description("The color of the lines of the blocks being rendered.")
        .defaultValue(new SettingColor(255, 0, 255, 255))
        .visible(() -> render.get())
        .build()
    );

    private final Setting<Keybind> cliclTP = sgGeneral.add(new KeybindSetting.Builder()
        .name("Keybind to tp")
        .description("Cancels sending packets and sends you back to your original position.")
        .defaultValue(Keybind.none())
        .action(() -> {
            try {
                Camera camera = mc.gameRenderer.getCamera();
                Vec3d cameraPos = camera.getPos();
                BlockPos pos = mc.world.raycast(new RaycastContext(cameraPos, cameraPos.add(Vec3d.fromPolar(camera.getPitch(), camera.getYaw()).multiply(197.0)), RaycastContext.ShapeType.OUTLINE, RaycastContext.FluidHandling.NONE, mc.player)).getBlockPos();
                int maxSearchHeight = 256; // Maximum height to search for a valid 2-block gap.
                BlockPos validPos = null;
                for (int y = pos.getY(); y < pos.getY() + maxSearchHeight; y++) {
                    BlockPos temppos = new BlockPos(pos.getX(), y, pos.getZ());
                    if (mc.player.getHeight() < 0.9) {
                        validPos = temppos.up();
                        break;
                    }
                    if (mc.world.isAir(temppos) && mc.world.isAir(temppos.up())) {
                        validPos = temppos;
                        break;
                    }
                }
                if (validPos != null && isChunkLoaded(validPos)) {
                    Vec3d toPos = new Vec3d(validPos.getX() + 0.5, validPos.getY(), validPos.getZ() + 0.5);
                    ChatUtils.info("Distance: " + mc.player.getPos().distanceTo(toPos));
                    Movement.moveTo(toPos);
                } else {
                    ChatUtils.error("No valid position found.");
                }
            } catch (Exception e) {
                ChatUtils.error("No valid position found.");
            }
        })
        .build()
    );


    public ClickTp() {
        super(AutoplayAddon.autoplay, "click-tp", "its clicktp");
    }
    @EventHandler
    private void onRender(Render3DEvent event) {
        Camera camera = mc.gameRenderer.getCamera();
        Vec3d cameraPos = camera.getPos();
        BlockPos pos = mc.world.raycast(new RaycastContext(cameraPos, cameraPos.add(Vec3d.fromPolar(camera.getPitch(), camera.getYaw()).multiply(300.0)), RaycastContext.ShapeType.OUTLINE, RaycastContext.FluidHandling.NONE, mc.player)).getBlockPos();
        int maxSearchHeight = 256; // Maximum height to search for a valid 2-block gap.
        BlockPos validPos = null;
        for (int y = pos.getY(); y < pos.getY() + maxSearchHeight; y++) {
            BlockPos temppos = new BlockPos(pos.getX(), y, pos.getZ());
            if (mc.player.getHeight() < 0.9) {
                validPos = temppos.up();
                break;
            }
            if (mc.world.isAir(temppos) && mc.world.isAir(temppos.up())) {
                validPos = temppos;
                break;
            }
        }
        if (validPos == null) return;
        double x1 = validPos.getX();
        double y1 = validPos.getY();
        double z1 = validPos.getZ();
        double x2 = x1 + 1;
        double y2 = y1 + 1;
        double z2 = z1 + 1;

        if (render.get()) {
            event.renderer.box(x1, y1, z1, x2, y2, z2, sideColor.get(), lineColor.get(), shapeMode.get(), 0);
        }
    }

    private boolean isChunkLoaded(BlockPos pos) {
        return mc.world.isChunkLoaded(pos.getX() >> 4, pos.getZ() >> 4);
    }

}



