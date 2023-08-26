package AutoplayAddon.AutoPlay.Movement;
import com.google.common.collect.ImmutableList;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import net.minecraft.util.math.*;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import AutoplayAddon.AutoPlay.Other.FastBox;
import java.util.List;
import static meteordevelopment.meteorclient.MeteorClient.mc;
public class CanTeleport {

    public static double searchY(Vec3d from, Vec3d to) {
        double fromy = from.y;
        if (tryY(from, to, fromy)) {
            // id if current player y works
            return -1337;
        }
        double ytotest = 0;
        boolean validTeleportFound = false;
        int searchOffset = 0;
        while (!validTeleportFound) {
            ytotest = (fromy + searchOffset);
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
        Box box = new Box(fromWithOffset.x - mc.player.getWidth() / 2, fromWithOffset.y, fromWithOffset.z - mc.player.getWidth() / 2, fromWithOffset.x + mc.player.getWidth() / 2, fromWithOffset.y + mc.player.getHeight(), fromWithOffset.z + mc.player.getWidth() / 2);
        return mc.world.isSpaceEmpty(box) && oldCheck(fromWithOffset, toWithOffset);
    }

    public static boolean lazyCheck(Vec3d from, Vec3d to) {

        if (to == null) {
            return false;
        }

        //Box oldBox = new Box(from.x - mc.player.getWidth() / 2, from.y, from.z - mc.player.getWidth() / 2, from.x + mc.player.getWidth() / 2, from.y + mc.player.getHeight(), from.z + mc.player.getWidth() / 2);
        Box newBox = new Box(to.x - mc.player.getWidth() / 2, to.y, to.z - mc.player.getWidth() / 2, to.x + mc.player.getWidth() / 2, to.y + mc.player.getHeight(), to.z + mc.player.getWidth() / 2);
        if (!mc.world.isSpaceEmpty(newBox)) {
            return false;
        }
        FastBox fastOldBox = new FastBox(from);

        Vec3d movement = new Vec3d(to.x - from.x, to.y - from.y, to.z - from.z);
        Vec3d test = adjustMovementForCollisions(fastOldBox, movement);
        Vec3d wentto = new Vec3d(from.x + test.x, from.y + test.y, from.z + test.z);
        if (!movement.equals(test)) {
           ChatUtils.error("does not equal movement " + movement + " test " + test);
        }

        double d6 = to.x - wentto.x;
        double d7 = to.y - wentto.y;

        if (d7 > -0.5D || d7 < 0.5D) {
            d7 = 0.0D;
        }
        double d8 = to.z - wentto.z;
        double d10 = d6 * d6 + d7 * d7 + d8 * d8;
        return !(d10 > 0.0625);
    }



    public static Vec3d adjustMovementForCollisions(FastBox fastBox, Vec3d movement) {
       // ChatUtils.info("check starting " + System.currentTimeMillis());
        if (movement.lengthSquared() == 0.0) return movement;
        double d = movement.x;
        double e = movement.y;
        double f = movement.z;
        if (e != 0.0) {
            e = calculateMaxOffset(fastBox, Direction.Axis.Y, e);
            if (e != 0.0) {
                fastBox.offset(new Vec3d(0.0, e, 0.0));
            }
        }

        boolean bl = Math.abs(d) < Math.abs(f);
        if (bl && f != 0.0) {
            f = calculateMaxOffset(fastBox, Direction.Axis.Z, f);
            if (f != 0.0) {
                fastBox.offset(new Vec3d(0.0, 0.0, f));
            }
        }
        if (d != 0.0) {
            d = calculateMaxOffset(fastBox, Direction.Axis.X, d);
            if (!bl && d != 0.0) {
                fastBox.offset(new Vec3d(d, 0.0, 0.0));
            }
        }
        if (!bl && f != 0.0) {
            f = calculateMaxOffset(fastBox, Direction.Axis.Z, f);
        }
        return new Vec3d(d, e, f);
    }

    public static Vec3d adjustMovementForCollisionsold(Box box, Vec3d movement) {
        // ChatUtils.info("check starting " + System.currentTimeMillis());
        if (movement.lengthSquared() == 0.0) return movement;
        List<VoxelShape> entityCollisions = mc.player.getWorld().getEntityCollisions(mc.player, box.stretch(movement));
        ImmutableList.Builder<VoxelShape> builder = ImmutableList.builderWithExpectedSize(entityCollisions.size() + 1);
        if (!entityCollisions.isEmpty()) {
            builder.addAll(entityCollisions);
        }
        builder.addAll(mc.player.getWorld().getBlockCollisions(mc.player, box.stretch(movement)));
        List<VoxelShape> allCollisions = builder.build();
        //ChatUtils.info("midway point collisions " + allCollisions.size() + " time " + System.currentTimeMillis());
        if (allCollisions.isEmpty()) return movement;
        double d = movement.x;
        double e = movement.y;
        double f = movement.z;
        if (e != 0.0) {
            e = VoxelShapes.calculateMaxOffset(Direction.Axis.Y, box, allCollisions, e);
            if (e != 0.0) {
                box = box.offset(0.0, e, 0.0);
            }
        }

        boolean bl = Math.abs(d) < Math.abs(f);
        if (bl && f != 0.0) {
            f = VoxelShapes.calculateMaxOffset(Direction.Axis.Z, box, allCollisions, f);
            if (f != 0.0) {
                box = box.offset(0.0, 0.0, f);
            }
        }

        if (d != 0.0) {
            d = VoxelShapes.calculateMaxOffset(Direction.Axis.X, box, allCollisions, d);
            if (!bl && d != 0.0) {
                box = box.offset(d, 0.0, 0.0);
            }
        }

        if (!bl && f != 0.0) {
            f = VoxelShapes.calculateMaxOffset(Direction.Axis.Z, box, allCollisions, f);
        }
        return new Vec3d(d, e, f);
    }


    public static double calculateMaxOffset(FastBox fastBox, Direction.Axis axis, double maxDist) {
        double step = 0.3;
        double currentOffset = 0.0;
        double lastValidOffset = 0.0;

        Vec3d direction = Vec3d.ZERO;
        switch(axis) {
            case X:
                direction = new Vec3d(1, 0, 0);
                break;
            case Y:
                direction = new Vec3d(0, 1, 0);
                break;
            case Z:
                direction = new Vec3d(0, 0, 1);
                break;
        }

        FastBox tempBox = new FastBox(fastBox);
        while (Math.abs(currentOffset) < Math.abs(maxDist)) {
            double offsetThisStep = step;

            // Check if we are about to overshoot the maxDist
            if (Math.abs(currentOffset + offsetThisStep) > Math.abs(maxDist)) {
                offsetThisStep = maxDist - currentOffset;
            }

            tempBox.offset(direction.multiply(offsetThisStep));

            if (tempBox.isPlayerCollidingWithBlocks()) {
                return lastValidOffset;
            } else {
                lastValidOffset = currentOffset;
            }

            currentOffset += offsetThisStep;
        }
        return maxDist;
    }


    public static boolean oldCheck(Vec3d from, Vec3d to) {

        if (to == null) {
            return false;
        }
        Box oldBox = new Box(from.x - mc.player.getWidth() / 2, from.y, from.z - mc.player.getWidth() / 2, from.x + mc.player.getWidth() / 2, from.y + mc.player.getHeight(), from.z + mc.player.getWidth() / 2);
        Box newBox = new Box(to.x - mc.player.getWidth() / 2, to.y, to.z - mc.player.getWidth() / 2, to.x + mc.player.getWidth() / 2, to.y + mc.player.getHeight(), to.z + mc.player.getWidth() / 2);
        if (!mc.world.isSpaceEmpty(newBox)) {
            return false;
        }

        double d6 = to.x - from.x;
        double d7 = to.y - from.y;
        double d8 = to.z - from.z;
        Vec3d test = adjustMovementForCollisionsold(oldBox, new Vec3d(d6, d7, d8));
        Vec3d wentto = new Vec3d(from.x + test.x, from.y + test.y, from.z + test.z);
        d6 = to.x - wentto.x;
        d7 = to.y - wentto.y;

        if (d7 > -0.5D || d7 < 0.5D) {
            d7 = 0.0D;
        }

        d8 = to.z - wentto.z;
        double d10 = d6 * d6 + d7 * d7 + d8 * d8;

        if (d10 > 0.0625) {
            return false;
        }

        return true;
    }


}
