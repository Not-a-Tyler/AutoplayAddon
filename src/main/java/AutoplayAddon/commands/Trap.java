package AutoplayAddon.commands;
import AutoplayAddon.AutoPlay.Movement.GotoUtil;
import AutoplayAddon.AutoPlay.Movement.MoveToUtil;
import AutoplayAddon.AutoPlay.Other.FastBox;
import AutoplayAddon.Tracker.ServerSideValues;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.commands.arguments.PlayerArgumentType;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.command.CommandSource;
import net.minecraft.entity.player.PlayerEntity;
import meteordevelopment.meteorclient.commands.Command;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;
import static meteordevelopment.meteorclient.MeteorClient.mc;


public class Trap extends Command {
    public Trap() {
        super("trap", "ban");
        MeteorClient.EVENT_BUS.subscribe(this);
    }
    List<BlockPos> trapBlocks;
    PlayerEntity e;
    Boolean trapping = false;
    @Override
    public void build(LiteralArgumentBuilder<CommandSource> builder) {
        builder.then(argument("player", PlayerArgumentType.create()).executes(context -> {
            e = PlayerArgumentType.get(context);
            trapBlocks = getTrap(e.getBoundingBox());
            ChatUtils.info("Trapping " + e.getName() + " with " + trapBlocks.size() + " blocks");
            GotoUtil.init(false, true);
            attemptTrap(e);
            return SINGLE_SUCCESS;
        }));
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (trapping) {
            attemptTrap(e);
        }
    }

    private void attemptTrap(PlayerEntity e) {
        Thread waitForTickEventThread1 = new Thread(() -> {
            Vec3d startingPos = mc.player.getPos();
            GotoUtil.setPos(e.getPos());
            ChatUtils.info("we need to place " + trapBlocks.size() + " blocks");
            while (!trapBlocks.isEmpty()) {
                BlockPos pos = trapBlocks.get(0);
                if (mc.world.getBlockState(pos).isSolid()) {
                    trapBlocks.remove(pos);
                    continue;
                }
                if (ServerSideValues.canPlace()) {
                    mc.getNetworkHandler().sendPacket(new PlayerInteractBlockC2SPacket(Hand.MAIN_HAND, new BlockHitResult(pos.toCenterPos(), Direction.UP, pos, false), 0));
                    ChatUtils.info("Placed block at " + pos.toShortString() + " on " + System.currentTimeMillis());
                    trapBlocks.remove(pos);
                } else {
                    trapping = true;
                    return;
                }
            }
            ChatUtils.info("Finished trapping");
            trapping = false;
            GotoUtil.setPos(startingPos);
            GotoUtil.disable();
        });
        waitForTickEventThread1.start();
    }

    private List<BlockPos> getTrap(Box box) {
        FastBox fastBox = new FastBox(box);
        List<BlockPos> collidedBlocks = fastBox.getOccupiedBlockPos();
        List<BlockPos> trapBlocks = new ArrayList<>();
        // Find the highest and lowest Y values of the collided blocks
        int minY = collidedBlocks.stream().min(Comparator.comparingInt(BlockPos::getY)).orElseThrow().getY();
        int maxY = collidedBlocks.stream().max(Comparator.comparingInt(BlockPos::getY)).orElseThrow().getY();

        // Determine the middle Y position between top and bottom of the entity
        int middleY = minY + (maxY - minY) / 2;

        // Create a ring halfway between top and bottom
        for (BlockPos blockPos : collidedBlocks) {
            if (blockPos.getY() == middleY) {
                for (Direction dir : Direction.values()) {
                    if (dir != Direction.UP && dir != Direction.DOWN) {
                        BlockPos offsetPos = blockPos.offset(dir);
                        if (!collidedBlocks.contains(offsetPos)) {
                            trapBlocks.add(offsetPos);
                        }
                    }
                }
            }
        }

        // Add blocks above and below the blocks that have the highest and lowest Y-values
        for (BlockPos blockPos : collidedBlocks) {
            if (blockPos.getY() == maxY) {
                trapBlocks.add(blockPos.up());
            }
            if (blockPos.getY() == minY) {
                trapBlocks.add(blockPos.down());
            }
        }
        return trapBlocks;
    }
}
