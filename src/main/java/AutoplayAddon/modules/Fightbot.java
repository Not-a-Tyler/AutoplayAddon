package AutoplayAddon.modules;
import java.math.BigDecimal;
import java.math.RoundingMode;

import AutoplayAddon.AutoPlay.Movement.GotoUtil;
import AutoplayAddon.AutoplayAddon;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import meteordevelopment.meteorclient.settings.*;
import java.util.Random;

public class Fightbot extends Module {
    private double lastAngle = 0.0;

    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    public Fightbot() {
        super(AutoplayAddon.autoplay, "fight-bot", "Example");
    }
    private final Setting<String> stringtype = sgGeneral.add(new StringSetting.Builder()
        .name("player-name")
        .description("Player to follow.")
        .defaultValue("")
        .build());

    private final Setting<Integer> distance = sgGeneral.add(new IntSetting.Builder()
        .name("distance-from-player")
        .description("How much distance in each teleport?")
        .defaultValue(20)
        .min(0)
        .sliderMax(300)
        .build());


    @Override
    public void onActivate() {
        GotoUtil.init(true, true);
    }
    @Override
    public void onDeactivate() {
        GotoUtil.disable();
    }
    int attackTick = 0;

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (mc.player == null) return;
        String targetName = stringtype.get();
        if (targetName.isEmpty()) return;
        for (Entity entity : mc.world.getEntities()) {
            if (entity instanceof PlayerEntity targetPlayer) {
                if (targetName.equals(targetPlayer.getGameProfile().getName())) {
                    attackTick++;
                    if (attackTick > 20) {
                        attackTick = 0;
                        mc.player.setPosition(targetPlayer.getPos());
                        new Thread(() -> {
                            ChatUtils.info("fightiung");
                            GotoUtil.setPos(targetPlayer.getPos());
                            mc.interactionManager.attackEntity(mc.player, targetPlayer);
                        }).start();
                        handlePlayerMovement(targetPlayer);
                    } else {
                        handlePlayerMovement(targetPlayer);
                    }
                    break;
                }
            }
        }
    }

    private void handlePlayerMovement(PlayerEntity targetPlayer) {
        Vec3d centerPos = roundToDecimal(targetPlayer.getPos(), 3);
        Vec3d desiredPos = getDesiredPositionBasedOnMode(centerPos);
        if (desiredPos != null) {
            new Thread(() -> {
                GotoUtil.setPos(desiredPos);
            }).start();
        }
    }


    private Vec3d getDesiredPositionBasedOnMode(Vec3d centerPos) {
        Random random = new Random();
        while (true) {
            double phi = 2 * Math.PI * random.nextDouble();
            double theta = Math.acos(2 * random.nextDouble() - 1);
            double radius = distance.get();
            double dx = radius * Math.sin(theta) * Math.cos(phi);
            double dy = radius * Math.sin(theta) * Math.sin(phi);
            double dz = radius * Math.cos(theta);
            Vec3d tempPos = new Vec3d(centerPos.x + dx, centerPos.y + dy, centerPos.z + dz);
            if (isBoxEmpty(tempPos)) {
                return tempPos;
            }
        }
    }

    private Boolean isBoxEmpty(Vec3d pos) {
        Box box = new Box(
            pos.x - mc.player.getWidth() / 2,
            pos.y,
            pos.z - mc.player.getWidth() / 2,
            pos.x + mc.player.getWidth() / 2,
            pos.y + mc.player.getHeight(),
            pos.z + mc.player.getWidth() / 2
        );
        return mc.world.isSpaceEmpty(box);
    }

    private Vec3d roundToDecimal(Vec3d vector, int decimalPlaces) {
        double roundedX = roundToDecimal(vector.x, decimalPlaces);
        double roundedY = roundToDecimal(vector.y, decimalPlaces);
        double roundedZ = roundToDecimal(vector.z, decimalPlaces);
        return new Vec3d(roundedX, roundedY, roundedZ);
    }

    private double roundToDecimal(double value, int decimalPlaces) {
        BigDecimal bd = new BigDecimal(Double.toString(value));
        bd = bd.setScale(decimalPlaces, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

}

