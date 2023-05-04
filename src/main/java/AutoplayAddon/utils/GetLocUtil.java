package AutoplayAddon.utils;
import meteordevelopment.meteorclient.utils.player.PlayerUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.block.Block;
import net.minecraft.entity.ItemEntity;
import java.util.*;
import java.util.stream.Collectors;
import static meteordevelopment.meteorclient.MeteorClient.mc;
public class GetLocUtil {

    public static Optional<UUID> findEntitys(List<EntityType<?>> entityTypes, LivingEntity searchingEntity, double searchRadius) {
        ClientWorld clientWorld = MinecraftClient.getInstance().world;
        Vec3d searchingEntityPos = searchingEntity.getPos();
        BlockPos searchingEntityBlockPos = searchingEntity.getBlockPos();
        Box searchBox = createSearchBox(searchingEntityBlockPos, searchRadius);

        List<Entity> entities = clientWorld.getOtherEntities(searchingEntity, searchBox, entity -> entityTypes.contains(entity.getType()));

        return entities.stream()
            .min(Comparator.comparingDouble(e -> searchingEntityPos.squaredDistanceTo(e.getPos())))
            .map(Entity::getUuid);
    }


    public static List<BlockPos> findItemEntities(List<Item> targetItems, double searchRadius) {
        if (mc.player == null || mc.world == null) {
            return Collections.emptyList();
        }

        BlockPos playerBlockPos = mc.player.getBlockPos();
        Box searchBox = createSearchBox(playerBlockPos, searchRadius);

        return mc.world.getEntitiesByClass(ItemEntity.class, searchBox, itemEntity -> isInTargetItems(itemEntity, targetItems))
            .stream()
            .map(ItemEntity::getBlockPos)
            .collect(Collectors.toList());
    }


    private static boolean isInTargetItems(ItemEntity itemEntity, List<Item> targetItems) {
        return targetItems.stream().anyMatch(targetItem -> targetItem.equals(itemEntity.getStack().getItem()));
    }




    public static List<BlockPos> findBlocks(List<Block> targetBlocks, double searchRadius) {
        World world = mc.player.getEntityWorld();
        BlockPos playerPos = mc.player.getBlockPos();
        Vec3d playerVec = mc.player.getPos();
        List<BlockPos> blockPositions = new ArrayList<>();
        for (double x = -searchRadius; x <= searchRadius; x++) {
            for (double y = -searchRadius; y <= searchRadius; y++) {
                for (double z = -searchRadius; z <= searchRadius; z++) {
                    BlockPos currentPos = playerPos.add((int) x, (int) y, (int) z);
                    Block currentBlock = world.getBlockState(currentPos).getBlock();
                    if (targetBlocks.contains(currentBlock) && !isPositionOccupied(world, currentPos)) {
                        if (PlayerUtils.distanceTo(currentPos) <= searchRadius) {
                            blockPositions.add(currentPos);
                        }
                    }
                }
            }
        }
        blockPositions.sort(Comparator.comparingDouble(o -> o.getSquaredDistance(playerVec)));
        return blockPositions;
    }

    public static boolean isPositionOccupied(World world, BlockPos pos) {
        // Check for entities in the given position
        List<Entity> entities = world.getEntitiesByClass(Entity.class, new Box(pos), entity -> !(entity instanceof PlayerEntity));
        if (!entities.isEmpty()) {
            return true;
        }

        // Check if the player is in the given position
        Box playerBoundingBox = mc.player.getBoundingBox();
        Box expandedBoundingBox = playerBoundingBox.expand(0.1, 0.1, 0.1); // Expand the bounding box slightly to avoid edge cases

        if (expandedBoundingBox.intersects(new Box(pos, pos.add(1, 1, 1)))) {
            return true;
        }

        return false;
    }





    private static Box createSearchBox(BlockPos pos, double searchRadius) {
        return new Box(
            pos.getX() - searchRadius, pos.getY() - searchRadius, pos.getZ() - searchRadius,
            pos.getX() + searchRadius, pos.getY() + searchRadius, pos.getZ() + searchRadius
        );
    }

}
