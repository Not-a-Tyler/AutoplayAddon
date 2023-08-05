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

public class SpinBot extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private Vec3d lastPlayerPos;
    public SpinBot() {
        super(AutoplayAddon.autoplay, "spi-bot", "Example");
    }
    private final Setting<String> stringtype = sgGeneral.add(new StringSetting.Builder()
        .name("playername")
        .description("Player to follow.")
        .defaultValue("")
        .build()
    );

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

                    // calculate new position on the orbit
                    double angle = (System.currentTimeMillis() / 1000.0) * (spinsppeed.get() / 60.0) * 2 * Math.PI;
                    double dx = Math.cos(angle) * distance.get();
                    double dz = Math.sin(angle) * distance.get();
                    Vec3d desiredPos = new Vec3d(centerPos.x + dx, centerPos.y, centerPos.z + dz);

                    // check if space is empty
                    Box box = new Box(
                        desiredPos.x - mc.player.getWidth() / 2,
                        desiredPos.y,
                        desiredPos.z - mc.player.getWidth() / 2,
                        desiredPos.x + mc.player.getWidth() / 2,
                        desiredPos.y + mc.player.getHeight(),
                        desiredPos.z + mc.player.getWidth() / 2
                    );

                    if (mc.world.isSpaceEmpty(box)) {
                        new Thread(() -> {
                            new GotoUtil().moveto(desiredPos.x, desiredPos.y, desiredPos.z);
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

