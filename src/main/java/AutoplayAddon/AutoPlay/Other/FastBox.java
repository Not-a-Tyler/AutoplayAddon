package AutoplayAddon.AutoPlay.Other;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.BlockPos;
import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;
import static meteordevelopment.meteorclient.MeteorClient.mc;

public class FastBox {

    public List<Vec3d> corners;
    public FastBox(FastBox other, boolean bottomOnly) {
        if (bottomOnly) {
            List<Vec3d> bottomCorners = Arrays.asList(
                other.corners.get(0),  // bottom front left
                other.corners.get(1),  // bottom front right
                other.corners.get(6),  // bottom back left
                other.corners.get(7)   // bottom back right
            );
            this.corners = bottomCorners;
        } else {
            this.corners = new ArrayList<>(other.corners);
        }
    }
    public FastBox(Vec3d to) {
        Box box = new Box(to.x - 0.3, to.y, to.z - 0.3, to.x + 0.3, to.y + mc.player.getHeight(), to.z + 0.3);
        box.expand(-0.0625D);
        calculateCorners(box);
    }


    protected void calculateCorners(Box box) {
        double minX = box.minX;
        double minY = box.minY;
        double minZ = box.minZ;
        double maxX = box.maxX;
        double maxY = box.maxY;
        double maxZ = box.maxZ;

        double midY = minY + (maxY - minY) / 2;

        this.corners = Arrays.asList(
            new Vec3d(minX, minY, minZ),  // bottom front left
            new Vec3d(maxX, minY, minZ),  // bottom front right
            new Vec3d(minX, maxY, minZ),  // top front left
            new Vec3d(maxX, maxY, minZ),  // top front right
            new Vec3d(minX, midY, minZ),  // center front left
            new Vec3d(maxX, midY, minZ),  // center front right
            new Vec3d(minX, minY, maxZ),  // bottom back left
            new Vec3d(maxX, minY, maxZ),  // bottom back right
            new Vec3d(minX, maxY, maxZ),  // top back left
            new Vec3d(maxX, maxY, maxZ),  // top back right
            new Vec3d(minX, midY, maxZ),  // center back left
            new Vec3d(maxX, midY, maxZ)   // center back right
        );
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

    public boolean isCollidingWithBlocks() {
        for (Vec3d corner : this.corners) {
            BlockPos blockPos = vecToBlockPos(corner);
            if (mc.world.getBlockState(blockPos).isSolid()) {
                //Movement.fastBoxBadList.add(new FastBox(this, false));
                return true;
            }
        }
       // Movement.fastBoxList.add(new FastBox(this, false));
        return false;
    }
}
