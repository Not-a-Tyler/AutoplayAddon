package AutoplayAddon.AutoPlay.Locator;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class ValidPickupPoint {

    public static Vec3d findFitSpot(World world, BlockPos originalPos) {
        Vec3d fitSpot = checkFit(world, originalPos, originalPos, 0.5);
        if (fitSpot != null) {
            return fitSpot;
        }

        BlockPos[] surroundingPos = getSurroundingBlocks(originalPos);

        for (BlockPos pos : surroundingPos) {
            fitSpot = checkFit(world, pos, originalPos, 0.3);
            if (fitSpot != null) {
                return fitSpot;
            }
        }

        for (BlockPos pos : surroundingPos) {
            fitSpot = checkFit(world, pos.down(), originalPos, 0.3);
            if (fitSpot != null) {
                return fitSpot;
            }
        }

        // Check two blocks down
        for (BlockPos pos : surroundingPos) {
            fitSpot = checkFit(world, pos.down(2), originalPos, 0.3);
            if (fitSpot != null) {
                return fitSpot;
            }
        }

        return null;
    }

    public static BlockPos[] getSurroundingBlocks(BlockPos originalPos) {
        int x = originalPos.getX();
        int y = originalPos.getY();
        int z = originalPos.getZ();

        return new BlockPos[]{
            new BlockPos(x, y, z + 1), // North
            new BlockPos(x, y, z - 1), // South
            new BlockPos(x + 1, y, z), // East
            new BlockPos(x - 1, y, z), // West
            new BlockPos(x + 1, y, z + 1), // North-East
            new BlockPos(x - 1, y, z + 1), // North-West
            new BlockPos(x + 1, y, z - 1), // South-East
            new BlockPos(x - 1, y, z - 1)  // South-West
        };
    }


    private static Vec3d checkFit(World world, BlockPos pos, BlockPos originalPos, double offset) {
        if (isAir(world, pos) && isAir(world, pos.up())) {
            double offsetX = pos.getX() < originalPos.getX() ? 0.7 : (pos.getX() > originalPos.getX() ? 0.3 : 0.5);
            double offsetZ = pos.getZ() < originalPos.getZ() ? 0.7 : (pos.getZ() > originalPos.getZ() ? 0.3 : 0.5);
            return new Vec3d(pos.getX() + offsetX, pos.getY(), pos.getZ() + offsetZ);
        }
        return null;
    }

    private static boolean isAir(World world, BlockPos pos) {return world.isAir(pos);}
}
