package AutoplayAddon.modules;
import AutoplayAddon.AutoPlay.Movement.CanTeleport;
import AutoplayAddon.AutoPlay.Movement.MoveToUtil;
import AutoplayAddon.AutoPlay.Movement.Movement;
import AutoplayAddon.AutoplayAddon;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.misc.Keybind;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import net.minecraft.client.render.Camera;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
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


    private final Setting<Keybind> cancelBlink2 = sgGeneral.add(new KeybindSetting.Builder()
        .name("Keybind to tp2")
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
            Movement.currentPosition = mc.player.getPos();
            MoveToUtil.moveTo(pos);
            mc.player.setPosition(pos.x, pos.y, pos.z);
        })
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

            ChatUtils.info(" lazyCheck: " + CanTeleport.lazyCheck(mc.player.getPos(), pos) + " slowCheck: " + CanTeleport.oldCheck(mc.player.getPos(), pos));
            ChatUtils.info(String.valueOf(System.currentTimeMillis()));
        })
        .build()
    );



    private final Setting<Keybind> cancelBlink1 = sgGeneral.add(new KeybindSetting.Builder()
        .name("interact test")
        .description("Cancels sending packets and sends you back to your original position.")
        .defaultValue(Keybind.none())
        .action(() -> {
            Camera camera = mc.gameRenderer.getCamera();
            Vec3d cameraPos = camera.getPos();
            float pitch = camera.getPitch();
            float yaw = camera.getYaw();
            Vec3d rotationVec = Vec3d.fromPolar(pitch, yaw);
            Vec3d raycastEnd = cameraPos.add(rotationVec.multiply(300.0));
            BlockPos blockpos = mc.world.raycast(new RaycastContext(cameraPos, raycastEnd, RaycastContext.ShapeType.OUTLINE, RaycastContext.FluidHandling.NONE, mc.player)).getBlockPos();
            mc.getNetworkHandler().sendPacket(new PlayerInteractBlockC2SPacket(Hand.MAIN_HAND, new BlockHitResult(blockpos.toCenterPos(), Direction.UP, blockpos, false), 0));
        })
        .build()
    );


    public SimpleClickTp() {
        super(AutoplayAddon.autoplay, "simple-click-tp", "its clicktp");
    }



}



