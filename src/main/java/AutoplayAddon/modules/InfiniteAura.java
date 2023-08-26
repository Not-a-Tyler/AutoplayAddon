package AutoplayAddon.modules;
import AutoplayAddon.AutoPlay.Movement.AIDS;
import AutoplayAddon.AutoPlay.Movement.Movement;
import AutoplayAddon.AutoplayAddon;
import meteordevelopment.meteorclient.settings.EntityTypeListSetting;
import meteordevelopment.meteorclient.settings.KeybindSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.friends.Friends;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.misc.Keybind;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
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




    private final Setting<Keybind> test = sgGeneral.add(new KeybindSetting.Builder()
        .name("100 dollars")
        .description("Teleports you to the closest player to your crosshair.")
        .defaultValue(Keybind.none())
        .action(() -> {
            double closestAngle = Double.MAX_VALUE;
            Entity closestEntity = null;
            Vec3d viewVec = mc.cameraEntity.getRotationVec(1.0F);
            for (Entity entity : mc.world.getEntities()) {
                if (entities.get().contains(entity.getType()) && entity != mc.player) {
                    Entity otherEntity = entity;
                    Vec3d toOther = otherEntity.getPos().subtract(mc.cameraEntity.getPos()).normalize();
                    double angle = Math.acos(viewVec.dotProduct(toOther));

                    if (angle < closestAngle) {
                        closestAngle = angle;
                        closestEntity = otherEntity;
                    }
                }
            }
            if (closestEntity == null) return;
            Entity finalClosestEntity = closestEntity;
            Thread waitForTickEventThread1 = new Thread(() -> {
                ChatUtils.info("hittinh");
                Vec3d startingPos = mc.player.getPos();
                Boolean ignore = false;
                if (!Movement.AIDSboolean) {
                    ChatUtils.info("starting");
                    AIDS.init(false);
                } else {
                    ignore = true;
                }
                AIDS.setPos(finalClosestEntity.getPos());
                mc.interactionManager.attackEntity(mc.player, finalClosestEntity);
                AIDS.setPos(startingPos);
                if (!ignore) {
                    ChatUtils.info("ending");
                    AIDS.disable();
                }
            });
            waitForTickEventThread1.start();
        })
        .build()
    );

}
