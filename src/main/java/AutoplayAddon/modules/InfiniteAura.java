package AutoplayAddon.modules;
import AutoplayAddon.AutoPlay.Movement.GotoUtil;
import AutoplayAddon.AutoPlay.Movement.Movement;
import AutoplayAddon.AutoPlay.Other.FastBox;
import AutoplayAddon.AutoPlay.Other.Packet;
import AutoplayAddon.AutoplayAddon;
import AutoplayAddon.Mixins.ClientConnectionInvokerMixin;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.misc.Keybind;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import net.minecraft.client.render.Camera;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

import java.util.Set;

public class InfiniteAura  extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    public InfiniteAura() {
        super(AutoplayAddon.autoplay, "infinite-aura", "test");
    }
    private final SettingGroup sgTargeting = settings.createGroup("Targeting");


    private final Setting<Set<EntityType<?>>> entities = sgTargeting.add(new EntityTypeListSetting.Builder()
        .name("entities")
        .description("Entities to attack.")
        .onlyAttackable()
        .defaultValue(EntityType.PLAYER)
        .build()
    );


    private final Setting<Keybind> hitEnemuy = sgGeneral.add(new KeybindSetting.Builder()
        .name("attack")
        .description("Estimates the entity you are looking at and attacks it.")
        .defaultValue(Keybind.none())
        .action(() -> {
            Movement.fastBoxBadList.clear();
            Movement.fastBoxList.clear();

            double closestAngle = Double.MAX_VALUE;
            Entity closestEntity = null;

            Camera camera = mc.gameRenderer.getCamera();

// Compute pitch and yaw using tickDelta (for this example, I assume tickDelta to be 1.0F)
            float pitch = camera.getPitch() * 0.017453292F;  // Convert to radians
            float yaw = -camera.getYaw() * 0.017453292F;    // Convert to radians and negate

// Compute the view direction vector
            float h = MathHelper.cos(yaw);
            float i = MathHelper.sin(yaw);
            float j = MathHelper.cos(pitch);
            float k = MathHelper.sin(pitch);
            Vec3d viewVec = new Vec3d(i * j, -k, h * j);

            for (Entity entity : mc.world.getEntities()) {
                if (entities.get().contains(entity.getType()) && entity != mc.player && (mc.player.distanceTo(entity) < 4000)) {

                    // Calculate the normalized direction vector from the camera to the entity.
                    Vec3d toEntity = entity.getPos().subtract(camera.getPos()).normalize();

                    // Compute the angle between the camera's view direction and the direction to the entity.
                    double angle = Math.acos(Math.max(-1.0, Math.min(1.0, viewVec.dotProduct(toEntity))));

                    // Update the closest entity if the current one has a smaller angle.
                    if (angle < closestAngle) {
                        closestAngle = angle;
                        closestEntity = entity;
                    }
                }
            }

// Handle the closest entity if it was found.
            if (closestEntity == null) return;
            Entity finalClosestEntity = closestEntity;
            ;



            FastBox fastBox = new FastBox(finalClosestEntity.getPos());
            if (fastBox.isCollidingWithBlocks()) {
                ChatUtils.error("Cannot directly teleport to entity, I still need to do like valid pos stuff");
                return;
            }
            Thread waitForTickEventThread1 = new Thread(() -> {
/*                ChatUtils.info("");
                ChatUtils.info("");*/
                ChatUtils.info("hitting " + finalClosestEntity.getName().getString());
                Vec3d startingPos = mc.player.getPos();
                boolean ignore;
                if (Movement.AIDSboolean) {
                    ignore = false;
                } else {
                    ignore = true;
                }

                if (ignore) GotoUtil.init();
                GotoUtil.setPos(finalClosestEntity.getPos(), true, true, false);
                Packet.sendPacket(PlayerInteractEntityC2SPacket.attack(finalClosestEntity, false));
                GotoUtil.setPos(startingPos, false, true, false);
                if (ignore) GotoUtil.disable();
                ChatUtils.info("Hit " + finalClosestEntity.getName().getString());
                //ChatUtils.info("");
                //ChatUtils.info("");
            });
            waitForTickEventThread1.start();
        })
        .build()
    );

}
