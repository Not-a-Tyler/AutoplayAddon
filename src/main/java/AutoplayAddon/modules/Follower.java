package AutoplayAddon.modules;
import java.math.BigDecimal;
import java.math.RoundingMode;

import AutoplayAddon.AutoPlay.Movement.GotoUtil;
import AutoplayAddon.AutoPlay.Movement.Movement;
import AutoplayAddon.AutoplayAddon;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import meteordevelopment.meteorclient.settings.*;
import java.util.Random;

public class Follower extends Module {
    private double lastAngle = 0.0;

    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    public Follower() {
        super(AutoplayAddon.autoplay, "follower", "Example");
    }
    private final Setting<String> stringtype = sgGeneral.add(new StringSetting.Builder()
        .name("player-name")
        .description("Player to follow.")
        .defaultValue("")
        .build()
    );

    private final Setting<Mode> mode = sgGeneral.add(new EnumSetting.Builder<Mode>()
        .name("mode")
        .description("The follow mode.")
        .defaultValue(Mode.HorizontalCircle)
        .build()
    );

    private final Setting<Integer> distance = sgGeneral.add(new IntSetting.Builder()
        .name("distance-from-player")
        .description("How much distance in each teleport?")
        .defaultValue(20)
        .min(0)
        .sliderMax(300)
        .build());

    private final Setting<Integer> spinsppeed = sgGeneral.add(new IntSetting.Builder()
        .name("speed")
        .description("How much distance in each teleport?")
        .defaultValue(20)
        .min(1)
        .sliderMax(300)
        .build());

    public enum Mode {
        Follow,
        StareAt,
        UpandDown,
        VerticalCircle,
        HorizontalCircle,
        RandomSphere
    }

    @Override
    public void onActivate() {
        GotoUtil.init(true, true);
    }
    @Override
    public void onDeactivate() {
        GotoUtil.disable();
    }




    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (mc.player == null) return;

        String targetName = stringtype.get();
        if (targetName.isEmpty()) return;
        try {
            for (Entity entity : mc.world.getEntities()) {
                if (entity instanceof PlayerEntity targetPlayer) {
                    if (targetName.equals(targetPlayer.getGameProfile().getName())) {
                        handlePlayerMovement(targetPlayer);
                        break;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void handlePlayerMovement(PlayerEntity targetPlayer) {
        Vec3d centerPos = roundToDecimal(targetPlayer.getPos(), 3);
        Vec3d desiredPos;

        if (mode.get() == Mode.StareAt) {
            desiredPos = getDesiredStareAtPosition(targetPlayer, centerPos);
        } else if (mode.get() == Mode.Follow) {
            desiredPos = centerPos;
        } else {
            desiredPos = getDesiredPositionBasedOnMode(centerPos);
        }

        if (desiredPos != null) {
            Movement.moveTo(desiredPos);
        }
    }

    private Vec3d getDesiredStareAtPosition(PlayerEntity targetPlayer, Vec3d centerPos) {
        Vec3d lookDirection = targetPlayer.getRotationVector();
        for (int i = distance.get(); i >= 0; i--) {
            Vec3d tempPos = centerPos.add(lookDirection.multiply(i));
            if (isBoxEmpty(tempPos)) return tempPos;
        }
        return null;
    }

    private Vec3d getDesiredPositionBasedOnMode(Vec3d centerPos) {
        double incrementInRadians = spinsppeed.get() * 0.00175;
        lastAngle = (lastAngle + incrementInRadians) % (2 * Math.PI);

        Vec3d tempPos = getPositionBasedOnAngleAndMode(centerPos, lastAngle);
        if (isBoxEmpty(tempPos)) {
            return tempPos;
        }

        // Fallback: If the new position isn't safe, try to find a safe one by iterating over angles
        double initialAngle = lastAngle;
        double angle = initialAngle;
        int maxIterations = 36;  // This is equivalent to trying every 10 degrees.
        int iterationCount = 0;

        while (iterationCount < maxIterations) {
            tempPos = getPositionBasedOnAngleAndMode(centerPos, angle);
            if (isBoxEmpty(tempPos)) {
                // Update lastAngle with the current angle before returning
                lastAngle = angle;
                return tempPos;
            }

            // Increment angle
            angle = (angle + Math.PI / 36) % (2 * Math.PI);
            iterationCount++;
        }

        return null;  // Return null if no safe position is found after trying all possible angles
    }



    private Vec3d getPositionBasedOnAngleAndMode(Vec3d centerPos, double angle) {
        if (mode.get() == Mode.RandomSphere) {
            Random random = new Random();
            double phi = 2 * Math.PI * random.nextDouble();
            double theta = Math.acos(2 * random.nextDouble() - 1);
            double radius = distance.get();
            double dx = radius * Math.sin(theta) * Math.cos(phi);
            double dy = radius * Math.sin(theta) * Math.sin(phi);
            double dz = radius * Math.cos(theta);
            return new Vec3d(centerPos.x + dx, centerPos.y + dy, centerPos.z + dz);
        }

        double dx = Math.cos(angle) * distance.get();
        double dz = Math.sin(angle) * distance.get();

        if (mode.get() == Mode.HorizontalCircle) {
            return new Vec3d(centerPos.x + dx, centerPos.y, centerPos.z + dz);
        } else if (mode.get() == Mode.VerticalCircle) {
            return new Vec3d(centerPos.x, centerPos.y + dx, centerPos.z + dz);
        } else if (mode.get() == Mode.UpandDown) {
            return new Vec3d(centerPos.x, centerPos.y + dx, centerPos.z);
        }

        // If none of the modes match, return the center position as a fallback.
        return centerPos;
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

