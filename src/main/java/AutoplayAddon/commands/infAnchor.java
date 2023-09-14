//package AutoplayAddon.commands;
//
//import AutoplayAddon.AutoPlay.Movement.GotoUtil;
//import AutoplayAddon.AutoPlay.Movement.MoveToUtil;
//import AutoplayAddon.AutoPlay.Movement.Movement;
//import com.mojang.brigadier.arguments.IntegerArgumentType;
//import com.mojang.brigadier.builder.LiteralArgumentBuilder;
//import meteordevelopment.meteorclient.commands.Command;
//import meteordevelopment.meteorclient.commands.arguments.PlayerArgumentType;
//import meteordevelopment.meteorclient.utils.player.ChatUtils;
//import net.minecraft.command.CommandSource;
//import net.minecraft.entity.Entity;
//import net.minecraft.entity.player.PlayerEntity;
//import net.minecraft.item.Item;
//import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
//import net.minecraft.util.Hand;
//import net.minecraft.util.hit.BlockHitResult;
//import net.minecraft.util.math.BlockPos;
//import net.minecraft.util.math.Box;
//import net.minecraft.util.math.Direction;
//import net.minecraft.util.math.Vec3d;
//import static com.mojang.brigadier.Command.SINGLE_SUCCESS;
//import static meteordevelopment.meteorclient.MeteorClient.mc;
//import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
//
//import java.util.List;
//
//public class infAnchor extends Command {
//    public infAnchor() {
//        super("infanchor", "ban");
//    }
//    private void switchToHotbar(Item targetItem) {
//        for (int i = 0; i < 9; i++) {
//            if (mc.player.getInventory().getStack(i).getItem() == targetItem) {
//                mc.getNetworkHandler().sendPacket(new UpdateSelectedSlotC2SPacket(i));
//            }
//        }
//    }
//
//    private boolean isValidPositionForAnchor(Vec3d pos) {
//        // 1. Check if block at the position is air (or replaceable)
////        if (!mc.world.getBlockState(new BlockPos(pos)).isAir()) {
//            retrn false;
//        }
//
//        // 2. Check if any entity's hitbox intersects with the block space
//        double minX = pos.x;
//        double minY = pos.y;
//        double minZ = pos.z;
//        double maxX = pos.x + 1; // Assuming block dimensions of 1x1x1
//        double maxY = pos.y + 1;
//        double maxZ = pos.z + 1;
//
//        List<Entity> entitiesInBox = mc.world.getOtherEntities(null, new Box(minX, minY, minZ, maxX, maxY, maxZ));
//        if (!entitiesInBox.isEmpty()) {
//            return false;
//        }
//
//        return true;
//    }
//
//
//    @Override
//    public void build(LiteralArgumentBuilder<CommandSource> builder) {
//        builder.then(argument("player", PlayerArgumentType.create()).executes(context -> {
//            PlayerEntity e = PlayerArgumentType.get(context);
//            Vec3d startingPos = mc.player.getPos();
//            Vec3d pos = e.getPos();
//            Vec3d finalPos1 = pos;
//            pos = pos.subtract(0, 3, 0);
//            Vec3d finalPos = pos;
//            Thread waitForTickEventThread1 = new Thread(() -> {
//                ChatUtils.info("hitting " + e.getName());
//                GotoUtil.init(false, false);
//                GotoUtil.setPos(finalPos);
//
//                BlockPos playerBlockPos = e.getBlockPos();
//                BlockPos[] potentialPositions = new BlockPos[] {
//                    playerBlockPos.down(),        // Directly below the player
//                    playerBlockPos.north(),       // North
//                    playerBlockPos.south(),       // South
//                    playerBlockPos.east(),        // East
//                    playerBlockPos.west()         // West
//                };
//
//                BlockPos finalBlockPos = null;
//                for (BlockPos potentialPos : potentialPositions) {
//                    if (isValidPositionForAnchor(potentialPos.toCenterPos())) {
//                        finalBlockPos = potentialPos;
//                        break;
//                    }
//                }
//
//                PlayerInteractBlockC2SPacket placePacket = new PlayerInteractBlockC2SPacket(Hand.OFF_HAND, new BlockHitResult(finalBlockPos.toCenterPos(), Direction.EAST, finalBlockPos, false), 0);
//
//                GotoUtil.setPos(startingPos);
//                GotoUtil.disable();
//                MoveToUtil.sendAllPacketsFromQueue();
//            });
//            waitForTickEventThread1.start();
//            return SINGLE_SUCCESS;
//        }));
//    }
//
//
//
//}
