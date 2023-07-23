package AutoplayAddon.modules;
import java.math.BigDecimal;
import java.math.RoundingMode;
import AutoplayAddon.AutoPlay.Movement.GotoUtil;
import AutoplayAddon.AutoplayAddon;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.settings.StringSetting;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;

public class Follower extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private Vec3d lastPlayerPos;
    public Follower() {
        super(AutoplayAddon.autoplay, "follower", "Example");
    }
    private final Setting<String> stringtype = sgGeneral.add(new StringSetting.Builder()
        .name("playername")
        .description("Player to follow.")
        .defaultValue("")
        .build()
    );



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
                    Vec3d targetPos = roundToDecimal(targetPlayer.getPos(), 3);
                    if (lastPlayerPos != null) {
                        Vec3d currentPosRounded = roundToDecimal(mc.player.getPos(), 3);
                        Vec3d lastTargetPosRounded = roundToDecimal(lastPlayerPos, 3);
                        if (!lastTargetPosRounded.equals(currentPosRounded)) {
                            new Thread(() -> {
                                new GotoUtil().moveto(targetPlayer.getX(), targetPlayer.getY(), targetPlayer.getZ());
                            }).start();
                        }
                    }
                    lastPlayerPos = targetPos; // Update the last known player position
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

