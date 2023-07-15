package AutoplayAddon.modules;
import AutoplayAddon.AutoPlay.Movement.GotoUtil;
import AutoplayAddon.AutoPlay.Other.WaitUtil;
import AutoplayAddon.AutoplayAddon;
import meteordevelopment.meteorclient.settings.EntityTypeListSetting;
import meteordevelopment.meteorclient.settings.KeybindSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.misc.Keybind;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.Camera;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

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
                Entity closestPlayer = null;
                Vec3d viewVec = player.getRotationVec(1.0F);

                for (Entity entity : mc.world.getEntities()) {
                    if (entities.get().contains(entity.getType()) && entity != player) {
                        PlayerEntity otherPlayer = (PlayerEntity) entity;
                        Vec3d toOther = otherPlayer.getPos().subtract(player.getPos()).normalize();
                        double angle = Math.acos(viewVec.dotProduct(toOther));

                        if (angle < closestAngle) {
                            closestAngle = angle;
                            closestPlayer = otherPlayer;
                        }
                    }
                }

                if (closestPlayer != null) {
                    Entity finalClosestPlayer = closestPlayer;
                    Thread waitForTickEventThread1 = new Thread(() -> {
                        Vec3d startingpos = mc.player.getPos();
                        Vec3d pos = finalClosestPlayer.getPos();
                        new GotoUtil().moveto(pos.x, pos.y, pos.z);
                        mc.interactionManager.attackEntity(mc.player, finalClosestPlayer);
                        mc.player.swingHand(Hand.MAIN_HAND);
                        new GotoUtil().moveto(startingpos.x, startingpos.y, startingpos.z);
                    });
                    waitForTickEventThread1.start();
                }
            }
        })
        .build()
    );


}
