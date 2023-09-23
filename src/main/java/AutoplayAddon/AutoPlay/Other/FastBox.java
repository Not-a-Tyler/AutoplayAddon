package AutoplayAddon.AutoPlay.Other;
import AutoplayAddon.AutoPlay.Movement.Movement;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.BlockPos;
import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;
import static meteordevelopment.meteorclient.MeteorClient.mc;

public class FastBox {

    public List<Vec3d> corners;
    public FastBox(FastBox other) {
        this.corners = new ArrayList<>(other.corners);
    }


    public void setPosition(Vec3d to) {
        Box box = new Box(to.x - 0.3, to.y, to.z - 0.3, to.x + 0.3, to.y + mc.player.getHeight(), to.z + 0.3);
        box.expand(-0.0625D);
        calculateCorners(box);
    }

    public FastBox(Vec3d to) {
        Box box = new Box(to.x - 0.3, to.y, to.z - 0.3, to.x + 0.3, to.y + mc.player.getHeight(), to.z + 0.3);
        box.expand(-0.0625D);
        calculateCorners(box);
    }
    public FastBox(Box box) {
        box.expand(-0.0625D);
        calculateCorners(box);
    }


    private void calculateCorners(Box box) {
        double minX = box.minX;
        double minY = box.minY;
        double minZ = box.minZ;
        double maxX = box.maxX;
        double maxY = box.maxY;
        double maxZ = box.maxZ;

        List<Double> xPoints = generatePoints(minX, maxX);
        List<Double> yPoints = generatePoints(minY, maxY);
        List<Double> zPoints = generatePoints(minZ, maxZ);

        List<Vec3d> allPoints = new ArrayList<>();

        for (double x : xPoints) {
            for (double y : yPoints) {
                for (double z : zPoints) {
                    allPoints.add(new Vec3d(x, y, z));
                }
            }
        }
        this.corners = allPoints;
    }

    private List<Double> generatePoints(double min, double max) {
        List<Double> points = new ArrayList<>();

        double diff = max - min;
        int numPoints = (int) Math.ceil(diff);

        // If the box is a single unit or smaller, just add the two ends.
        if (numPoints <= 1) {
            points.add(min);
            points.add(max);
            return points;
        }

        double interval = diff / numPoints;

        for (int i = 0; i <= numPoints; i++) {
            points.add(min + (interval * i));
        }

        return points;
    }



    private BlockPos vecToBlockPos(Vec3d vec) {
        return new BlockPos((int) Math.floor(vec.x), (int) Math.floor(vec.y), (int) Math.floor(vec.z));
    }

    public FastBox offset(Vec3d velocity) {
        for (int i = 0; i < corners.size(); i++) {
            Vec3d corner = corners.get(i);
            corners.set(i, corner.add(velocity));
        }
        return this;
    }

    public List<BlockPos> getOccupiedBlockPos() {
        List<BlockPos> occupiedBlocks = new ArrayList<>();
        for (Vec3d corner : this.corners) {
            BlockPos blockPos = vecToBlockPos(corner);
            if (!occupiedBlocks.contains(blockPos)) {
                occupiedBlocks.add(blockPos);
            }
        }
        return occupiedBlocks;
    }

    public boolean isCollidingWithBlocks() {
        List <BlockPos> cache = new ArrayList<>();
        //Movement.fastBoxList.add(new FastBox(this));
        for (Vec3d corner : this.corners) {
            BlockPos blockPos = vecToBlockPos(corner);
            if (cache.contains(blockPos)) {
                continue;
            }
            cache.add(blockPos);
            if (mc.world.getBlockState(blockPos).isSolid()) {
           //     Movement.fastBoxList.add(new FastBox(this));
                return true;
            }
        }
        return false;
    }
}
