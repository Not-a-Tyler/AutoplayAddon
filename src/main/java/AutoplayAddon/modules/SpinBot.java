package AutoplayAddon.modules;
import java.math.BigDecimal;
import java.math.RoundingMode;
import AutoplayAddon.AutoPlay.Movement.GotoUtil;
import AutoplayAddon.AutoplayAddon;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.settings.StringSetting;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import meteordevelopment.meteorclient.settings.*;
import java.util.Random;

public class SpinBot extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private Vec3d lastPlayerPos;
    public SpinBot() {
        super(AutoplayAddon.autoplay, "spin-bot", "Example");
    }
    private final Setting<String> stringtype = sgGeneral.add(new StringSetting.Builder()
        .name("playername")
        .description("Player to follow.")
        .defaultValue("")
        .build()
    );

    private final Setting<Mode> mode = sgGeneral.add(new EnumSetting.Builder<Mode>()
        .name("mode")
        .description("The mode for Flight.")
        .defaultValue(Mode.HorizontalCircle)
        .build()
    );


    public enum Mode {
        StareAt,
        UpandDown,
        VerticalCircle,
        HorizontalCircle,
        RandomSphere
    }


    private final Setting<Integer> distance = sgGeneral.add(new IntSetting.Builder()
        .name("Distance to spin around player")
        .description("How much distance in each teleport?")
        .defaultValue(20)
        .min(0)
        .sliderMax(300)
        .build());

    private final Setting<Integer> spinsppeed = sgGeneral.add(new IntSetting.Builder()
        .name("Speed to spin around player")
        .description("How much distance in each teleport?")
        .defaultValue(20)
        .min(0)
        .sliderMax(300)
        .build());



    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (mc.player == null) {
            return;
        }
        String targetName = stringtype.get(); // Get the player name from the setting

        if (targetName.isEmpty()) return;
        for (Entity entity : mc.world.getEntities()) {
            if (entity instanceof PlayerEntity) {
                PlayerEntity targetPlayer = (PlayerEntity) entity;
                if (targetName.equals(targetPlayer.getGameProfile().getName())) {
                    // determine center of orbit
                    Vec3d centerPos = roundToDecimal(targetPlayer.getPos(), 3);
                    if (mode.get() == Mode.StareAt) {
                        Vec3d lookDirection = targetPlayer.getRotationVector();
                        Vec3d desiredPos = null;
                        Box box = null;
                        boolean isSpaceEmpty = false;

                        for (int i = distance.get(); i >= 0; i--) {
                            desiredPos = centerPos.add(lookDirection.multiply(i));

                            // check if space is empty
                            box = new Box(
                                desiredPos.x - mc.player.getWidth() / 2,
                                desiredPos.y,
                                desiredPos.z - mc.player.getWidth() / 2,
                                desiredPos.x + mc.player.getWidth() / 2,
                                desiredPos.y + mc.player.getHeight(),
                                desiredPos.z + mc.player.getWidth() / 2
                            );

                            isSpaceEmpty = mc.world.isSpaceEmpty(box);

                            if (isSpaceEmpty) break;
                        }

                        if (!isSpaceEmpty) return; // No available spaces found

                        if (desiredPos != null) {
                            Vec3d finalDesiredPos = desiredPos;
                            new Thread(() -> {
                                new GotoUtil().moveto(finalDesiredPos.x, finalDesiredPos.y, finalDesiredPos.z);
                            }).start();
                        }

                        break;
                    }
                    // calculate new position on the orbit
                    double initialAngle = (System.currentTimeMillis() / 1000.0) * (spinsppeed.get() / 60.0) * 2 * Math.PI;
                    double angle = initialAngle;
                    Vec3d desiredPos = null;
                    Box box = null;
                    boolean isSpaceEmpty = false;



                    if (mode.get() != Mode.RandomSphere) {
                        do {
                            double dx = Math.cos(angle) * distance.get();
                            double dz = Math.sin(angle) * distance.get();

                            if (mode.get() == Mode.HorizontalCircle) {
                                desiredPos = new Vec3d(centerPos.x + dx, centerPos.y, centerPos.z + dz);
                            } else if (mode.get() == Mode.VerticalCircle) {
                                desiredPos = new Vec3d(centerPos.x, centerPos.y + dx, centerPos.z + dz);
                            } else { // Up and down mode
                                desiredPos = new Vec3d(centerPos.x, centerPos.y + dx, centerPos.z);
                            }

                            // check if space is empty
                            box = new Box(
                                desiredPos.x - mc.player.getWidth() / 2,
                                desiredPos.y,
                                desiredPos.z - mc.player.getWidth() / 2,
                                desiredPos.x + mc.player.getWidth() / 2,
                                desiredPos.y + mc.player.getHeight(),
                                desiredPos.z + mc.player.getWidth() / 2
                            );

                            isSpaceEmpty = mc.world.isSpaceEmpty(box);

                            // if space is not empty, adjust the angle and re-check
                            if (!isSpaceEmpty) {
                                angle = (angle + Math.PI / 36) % (2 * Math.PI); // Increase angle by 5 degrees

                                // Break if we've done a full circle and haven't found an empty space
                                if (angle == initialAngle) {
                                    return; // No available spaces found
                                }
                            }

                        } while (!isSpaceEmpty);
                    } else { // Random Sphere mode
                        Random random = new Random();

                        for (int i = 0; i < 360; i++) {
                            // Generate random spherical coordinates
                            double phi = 2 * Math.PI * random.nextDouble();
                            double theta = Math.acos(2 * random.nextDouble() - 1);
                            double radius = distance.get();

                            // Convert spherical coordinates to cartesian
                            double dx = radius * Math.sin(theta) * Math.cos(phi);
                            double dy = radius * Math.sin(theta) * Math.sin(phi);
                            double dz = radius * Math.cos(theta);

                            desiredPos = new Vec3d(centerPos.x + dx, centerPos.y + dy, centerPos.z + dz);

                            // check if space is empty
                            box = new Box(
                                desiredPos.x - mc.player.getWidth() / 2,
                                desiredPos.y,
                                desiredPos.z - mc.player.getWidth() / 2,
                                desiredPos.x + mc.player.getWidth() / 2,
                                desiredPos.y + mc.player.getHeight(),
                                desiredPos.z + mc.player.getWidth() / 2
                            );

                            if (mc.world.isSpaceEmpty(box)) {
                                isSpaceEmpty = true;
                                break;
                            }
                        }

                        if (!isSpaceEmpty) {
                            return; // No available spaces found
                        }
                    }

                    if (desiredPos != null) {
                        Vec3d finalDesiredPos = desiredPos;
                        new Thread(() -> {
                            new GotoUtil().moveto(finalDesiredPos.x, finalDesiredPos.y, finalDesiredPos.z);
                        }).start();
                    }

                    break;
                }
            }
        }
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

