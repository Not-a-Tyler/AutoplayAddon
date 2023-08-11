package AutoplayAddon.BotTest.Commands;

import AutoplayAddon.AutoPlay.Movement.GotoUtil;
import AutoplayAddon.BotTest.ArgumentType;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;
import java.math.BigDecimal;
import java.math.RoundingMode;
import static meteordevelopment.meteorclient.MeteorClient.mc;

public class FollowCommand {
    private boolean followMode = false;
    private String targetName = "";
    private Vec3d lastPlayerPos = null;
    private static ArgumentType.BooleanType booleanArgumentType = new ArgumentType.BooleanType();
    private ArgumentType.PlayerNameType playerNameType = new ArgumentType.PlayerNameType();

    public void processFollowCommand(String playerName, String argument) {
        if (argument == null || booleanArgumentType.parse(argument)) {
            followMode = true;
            targetName = playerName;
            ChatUtils.sendPlayerMsg("Following player " + targetName + ".");
        } else if ( booleanArgumentType.parse(argument) == false) {
            followMode = false;
            targetName = "";
            ChatUtils.sendPlayerMsg("Follow mode disabled.");
        } else {
            targetName =  playerNameType.parse(argument);
            ChatUtils.sendPlayerMsg("Now following player " + targetName);
        }
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (!followMode || targetName.isEmpty()) {
            return;
        }
        for (Entity entity : mc.world.getEntities()) {
            if (entity instanceof PlayerEntity) {
                PlayerEntity targetPlayer = (PlayerEntity) entity;
                if (targetName.equals(targetPlayer.getGameProfile().getName())) {
                    Vec3d targetPos = roundToDecimal(targetPlayer.getPos(), 3);
                    if (lastPlayerPos != null) {
                        Vec3d currentPosRounded = roundToDecimal(mc.player.getPos(), 3);
                        Vec3d lastTargetPosRounded = roundToDecimal(lastPlayerPos, 3);
                        if (!lastTargetPosRounded.equals(currentPosRounded)) {
                            GotoUtil.moveto(targetPlayer.getX(), targetPlayer.getY(), targetPlayer.getZ(), false);
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
