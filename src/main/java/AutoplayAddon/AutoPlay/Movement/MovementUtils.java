package AutoplayAddon.AutoPlay.Movement;

import AutoplayAddon.AutoplayAddon;
import AutoplayAddon.Tracker.ServerSideValues;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import net.minecraft.util.math.Vec3d;

import static meteordevelopment.meteorclient.MeteorClient.mc;

public class MovementUtils {

    public static boolean predictifPossible(Vec3d newPos) {
        int predict;
        double base = MovementUtils.findFarthestDistance(newPos);
        int packetsRequired = (int) Math.floor(Math.abs(base / 10.0));
        if (AutoplayAddon.values.hasMoved) {
            predict = ((packetsRequired + 1) * 2);
            ChatUtils.info("Predicted " + (ServerSideValues.delta() - predict) + " since player has moved");
        } else {
            predict = (packetsRequired + 2);
            ChatUtils.info("Predicted " + (ServerSideValues.delta() - predict) + " since player has not moved");
        }
        if (ServerSideValues.delta() < predict) {
            return false;
        } else {
            return true;
        }
    }
    public static double findFarthestDistance(Vec3d newPos) {
        Vec3d tickpos = AutoplayAddon.values.tickpos;
        Vec3d currPos = mc.player.getPos();
        return findFarthestDistanceArray(newPos, tickpos, currPos);
    }

    private static double findFarthestDistanceArray(Vec3d newPos, Vec3d... vec3dArray) {
        double maxDistance = Double.MIN_VALUE;

        for (Vec3d vec3d : vec3dArray) {
            double distance = newPos.distanceTo(vec3d);
            if (distance > maxDistance) {
                maxDistance = distance;
            }
        }
        return maxDistance;
    }
}
