package AutoplayAddon.modules;
import AutoplayAddon.AutoPlay.Movement.GotoUtil;
import AutoplayAddon.AutoPlay.Movement.MoveToUtil;
import AutoplayAddon.AutoplayAddon;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.misc.Keybind;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.render.Camera;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;


public class SimpleClickTp extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgRender = settings.createGroup("Render");


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

    private final Setting<Keybind> cancelBlink = sgGeneral.add(new KeybindSetting.Builder()
        .name("Keybind to tp")
        .description("Cancels sending packets and sends you back to your original position.")
        .defaultValue(Keybind.none())
        .action(() -> {
            Camera camera = mc.gameRenderer.getCamera();
            Vec3d cameraPos = camera.getPos();
            float pitch = camera.getPitch();
            float yaw = camera.getYaw();
            Vec3d rotationVec = Vec3d.fromPolar(pitch, yaw);
            Vec3d raycastEnd = cameraPos.add(rotationVec.multiply(300.0));
            BlockPos blockpos = mc.world.raycast(new RaycastContext(cameraPos, raycastEnd, RaycastContext.ShapeType.OUTLINE, RaycastContext.FluidHandling.NONE, mc.player)).getBlockPos().up();
            Vec3d pos = new Vec3d((blockpos.getX() + .5), blockpos.getY(), (blockpos.getZ() + .5));
            MoveToUtil.moveTo(pos);
        })
        .build()
    );


    public SimpleClickTp() {
        super(AutoplayAddon.autoplay, "simple-click-tp", "its clicktp");
    }
    @EventHandler
    private void onRender(Render3DEvent event) {
        Camera camera = mc.gameRenderer.getCamera();
        Vec3d cameraPos = camera.getPos();
        float pitch = camera.getPitch();
        float yaw = camera.getYaw();
        Vec3d rotationVec = Vec3d.fromPolar(pitch, yaw);
        Vec3d raycastEnd = cameraPos.add(rotationVec.multiply(300.0));
        BlockHitResult pos1 = mc.world.raycast(new RaycastContext(cameraPos, raycastEnd, RaycastContext.ShapeType.OUTLINE, RaycastContext.FluidHandling.NONE, mc.player));
        BlockPos location = pos1.getBlockPos();
        double x1 = location.getX();
        double y1 = location.getY() + 1;
        double z1 = location.getZ();
        double x2 = x1 + 1;
        double y2 = y1 + 1;
        double z2 = z1 + 1;

        if (render.get()) {
            event.renderer.box(x1, y1, z1, x2, y2, z2, sideColor.get(), lineColor.get(), shapeMode.get(), 0);
        }
    }



}



