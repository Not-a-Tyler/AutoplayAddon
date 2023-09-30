package AutoplayAddon.AutoPlay.Movement;
import AutoplayAddon.AutoPlay.Other.FastBox;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import net.minecraft.util.math.Vec3d;
import java.util.ArrayList;
import java.util.List;

public class Path {

    public static int searchY(Vec3d from, Vec3d to) {
        double fromy = from.y;
        if (tryY(from, to, fromy)) {
            // id if current player y works
            return -1337;
        }
        int ytotest = 0;
        boolean validTeleportFound = false;
        int searchOffset = 0;
        while (!validTeleportFound) {
            ytotest = (int) (fromy + searchOffset);
            if (tryY(from, to, ytotest)) {
                validTeleportFound = true;
            } else {
                searchOffset = (searchOffset <= 0) ? 1 - searchOffset : -searchOffset;
            }
        }
        return ytotest;
    }

    public static boolean tryY(Vec3d from, Vec3d to, double ytotest) {
        Vec3d fromWithOffset = new Vec3d(from.x, ytotest, from.z);
        Vec3d toWithOffset = new Vec3d(to.x, ytotest, to.z);
        FastBox fromBox = new FastBox(fromWithOffset);
        if (fromBox.isCollidingWithBlocks()) return false;
        return CanTeleport.lazyCheck(fromWithOffset, toWithOffset);
    }


    public static int calculatePackets(List<TeleportTask> tasks) {
        int packetsRequired = 0;
        for (TeleportTask task : tasks) {
            int packets = task.getPacketsRequired();
            if (packets > packetsRequired) {
                packetsRequired = packets;
            }
        }
        return packetsRequired;
    }


    private static Vec3d getStage(Vec3d from, Vec3d to, int stage, double y) {
        if (stage == 1) return new Vec3d(from.getX(), y, from.getZ());
        if (stage == 2) return new Vec3d(to.getX(), y, to.getZ());
        if (stage == 3) return new Vec3d(to.getX(), to.getY(), to.getZ());
        return new Vec3d(from.getX(), from.getY(), from.getZ());
    }

    public static List<TeleportTask> getPath(Vec3d from, Vec3d to) {
        List<TeleportTask> path = new ArrayList<>();
        int stage = 0;
        double y = -13377;
        Vec3d currentTempPos = from;
        while (true) {
            if (stage == 4) {
                ChatUtils.error("stage is 4");
                break;
            }
            if (CanTeleport.lazyCheck(currentTempPos, to)) {
                path.add(new TeleportTask(to, currentTempPos, path.size()));
                break;
            } else if (y == -13377) {
                // get a y value to path with
                y = searchY(from, to);
                if (y == -1337) {
                    y = from.y;
                    stage = 2;
                } else {
                    stage = 1;
                }
            }
            Vec3d newPos = getStage(currentTempPos, to, stage, y);
            path.add(new TeleportTask(newPos, currentTempPos, path.size()));
            currentTempPos = newPos;
            stage++;
        }
        return path;
    }
}
