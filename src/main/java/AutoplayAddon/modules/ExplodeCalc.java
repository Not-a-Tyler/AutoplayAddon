package AutoplayAddon.modules;

import AutoplayAddon.AutoplayAddon;
import meteordevelopment.meteorclient.events.entity.player.StartBreakingBlockEvent;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.settings.ColorSetting;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.util.math.*;
import net.minecraft.util.hit.HitResult;
import net.minecraft.world.RaycastContext;

public class ExplodeCalc extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgRender = settings.createGroup("Render");

    private final Setting<ShapeMode> shapeMode = sgRender.add(new EnumSetting.Builder<ShapeMode>()
        .name("shape-mode")
        .description("How the shapes are rendered.")
        .defaultValue(ShapeMode.Both)
        .build()
    );

    private final Setting<SettingColor> sideColor = sgRender.add(new ColorSetting.Builder()
        .name("side-color")
        .description("The color of the sides of the blocks being rendered.")
        .defaultValue(new SettingColor(204, 0, 0, 10))
        .build()
    );

    private final Setting<SettingColor> lineColor = sgRender.add(new ColorSetting.Builder()
        .name("line-color")
        .description("The color of the lines of the blocks being rendered.")
        .defaultValue(new SettingColor(204, 0, 0, 255))
        .build()
    );

    private BlockPos blockPos = new BlockPos(0, -1, 0);
    private Direction direction = Direction.UP;

    public ExplodeCalc() {
        super(AutoplayAddon.autoplay, "explode-calc", "Attempts to instantly mine blocks.");
    }

    @Override
    public void onActivate() {
        blockPos = null;
    }

    @EventHandler
    private void onStartBreakingBlock(StartBreakingBlockEvent event) {
        ChatUtils.info("Start breaking block event");
        direction = event.direction;
        blockPos = event.blockPos;
    }


    public float getExposure(Vec3d source, Box box) {
        double d = 1.0 / ((box.maxX - box.minX) * 2.0 + 1.0);
        double e = 1.0 / ((box.maxY - box.minY) * 2.0 + 1.0);
        double f = 1.0 / ((box.maxZ - box.minZ) * 2.0 + 1.0);
        double g = (1.0 - Math.floor(1.0 / d) * d) / 2.0;
        double h = (1.0 - Math.floor(1.0 / f) * f) / 2.0;
        if (!(d < 0.0) && !(e < 0.0) && !(f < 0.0)) {
            int i = 0;
            int j = 0;

            for(double k = 0.0; k <= 1.0; k += d) {
                for(double l = 0.0; l <= 1.0; l += e) {
                    for(double m = 0.0; m <= 1.0; m += f) {
                        double n = MathHelper.lerp(k, box.minX, box.maxX);
                        double o = MathHelper.lerp(l, box.minY, box.maxY);
                        double p = MathHelper.lerp(m, box.minZ, box.maxZ);
                        Vec3d vec3d = new Vec3d(n + g, o, p + h);
                        if (mc.world.raycast(new RaycastContext(vec3d, source, RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, mc.player)).getType() == HitResult.Type.MISS) {
                            ++i;
                        }

                        ++j;
                    }
                }
            }

            return (float)i / (float)j;
        } else {
            return 0.0F;
        }
    }


    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (blockPos == null) return;
        float damage = getAnchorDamage(blockPos, mc.player.getPos());
        ChatUtils.info("Damage: " + damage);

    }

    public float getAnchorDamage(BlockPos anchorpos, Vec3d playerPos) {
        Box playerPosBox = mc.player.getBoundingBox();
        float power = 5.0F;
        Vec3d vec3d = anchorpos.toCenterPos();
        float q = power * 2.0F;
        double w = Math.sqrt(playerPos.squaredDistanceTo(vec3d)) / (double)q;
        if (w <= 1.0) {
            double x = playerPos.getX() - vec3d.x;
            double eyeHeight = mc.player.getEyeHeight(mc.player.getPose());
            double playerEyeY = playerPos.y + eyeHeight;
            double y = playerEyeY - vec3d.y;
            double z = playerPos.getZ() - vec3d.z;
            double aa = Math.sqrt(x * x + y * y + z * z);
            if (aa != 0.0) {
                double ab = (double)getExposure(vec3d, playerPosBox);
                double ac = (1.0 - w) * ab;
                return ((float)((int)((ac * ac + ac) / 2.0 * 7.0 * (double)q + 1.0)));
            }
        }
        return 0.0F;
    }

    @EventHandler
    private void onRender(Render3DEvent event) {
        if (blockPos == null) return;
        event.renderer.box(blockPos, sideColor.get(), lineColor.get(), shapeMode.get(), 0);
    }

}
