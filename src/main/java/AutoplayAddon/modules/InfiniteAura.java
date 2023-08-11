package AutoplayAddon.modules;
import AutoplayAddon.AutoPlay.Movement.GotoUtil;
import AutoplayAddon.AutoplayAddon;
import meteordevelopment.meteorclient.settings.EntityTypeListSetting;
import meteordevelopment.meteorclient.settings.KeybindSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.misc.Keybind;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.util.Hand;
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

    private final Setting<Keybind> cancelBlink = sgGeneral.add(new KeybindSetting.Builder()
        .name("Keybind to tp")
        .description("Teleports you to the closest player to your crosshair.")
        .defaultValue(Keybind.none())
        .action(() -> {
            ClientPlayerEntity player = mc.player;
            if (player != null) {
                double closestAngle = Double.MAX_VALUE;
                Entity closestEntity = null;
                Vec3d viewVec = player.getRotationVec(1.0F);

                for (Entity entity : mc.world.getEntities()) {
                    if (entities.get().contains(entity.getType()) && entity != player) {
                        Entity otherEntity = entity;
                        Vec3d toOther = otherEntity.getPos().subtract(player.getPos()).normalize();
                        double angle = Math.acos(viewVec.dotProduct(toOther));

                        if (angle < closestAngle) {
                            closestAngle = angle;
                            closestEntity = otherEntity;
                        }
                    }
                }

                if (closestEntity != null) {
                    Entity finalClosestEntity = closestEntity;
                    Thread waitForTickEventThread1 = new Thread(() -> {
                        Vec3d startingpos = mc.player.getPos();
                        Vec3d pos = finalClosestEntity.getPos();
                        new GotoUtil().moveto(pos.x, pos.y, pos.z, true);
                        mc.interactionManager.attackEntity(mc.player, finalClosestEntity);
                        mc.player.swingHand(Hand.MAIN_HAND);
                        new GotoUtil().moveto(startingpos.x, startingpos.y, startingpos.z, true);
                    });
                    waitForTickEventThread1.start();
                }
            }
        })
        .build()
    );

}
