package AutoplayAddon.commands;
import AutoplayAddon.AutoPlay.Movement.GotoUtil;
import AutoplayAddon.AutoPlay.Movement.Movement;
import AutoplayAddon.AutoPlay.Other.FastBox;
import AutoplayAddon.Mixins.ClientConnectionInvokerMixin;
import AutoplayAddon.Tracker.ServerSideValues;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.commands.arguments.PlayerArgumentType;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.Blocks;

import net.minecraft.command.CommandSource;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import meteordevelopment.meteorclient.commands.Command;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.List;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;
import static meteordevelopment.meteorclient.MeteorClient.mc;


public class Trap extends Command {
    public Trap() {
        super("trap", "ban");
        MeteorClient.EVENT_BUS.subscribe(this);
    }
    List<BlockPos> trapBlocks;
    Vec3d startingPos;
    Boolean ignore = false;
    PlayerEntity e;
    Boolean trapping = false;
    @Override
    public void build(LiteralArgumentBuilder<CommandSource> builder) {
        builder.then(argument("player", PlayerArgumentType.create()).executes(context -> {
            startingPos = mc.player.getPos();
            e = PlayerArgumentType.get(context);
            trapBlocks = getTrap(e);
            ChatUtils.info("Trapping " + e.getName().getString() + " with " + trapBlocks.size() + " blocks " + System.currentTimeMillis());
            ignore = true;
            if (!Movement.AIDSboolean) {
                GotoUtil.init(false, true);
                ignore = false;
            }
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
            GotoUtil.setPos(e.getPos(), false);
            ChatUtils.info("we need to place " + trapBlocks.size() + " blocks ");

            List<BlockPos> toRemove = new ArrayList<>();
            for (BlockPos pos : trapBlocks) {
                if (mc.world.getBlockState(pos).isSolid()) {
                    toRemove.add(pos);
                    continue;
                }
                if (ServerSideValues.canPlace()) {
                    mc.world.setBlockState(pos, Blocks.GREEN_TERRACOTTA.getDefaultState());
                    PlayerInteractBlockC2SPacket packet = new PlayerInteractBlockC2SPacket(Hand.MAIN_HAND, new BlockHitResult(pos.toCenterPos(), Direction.UP, pos, false), 0);
                    ((ClientConnectionInvokerMixin) mc.getNetworkHandler().getConnection())._sendImmediately(packet, null);
                    ServerSideValues.handleUse();
                    toRemove.add(pos);
                } else {
                    trapping = true;
                    return;
                }
            }
            trapBlocks.removeAll(toRemove);
            trapping = false;
            GotoUtil.setPos(startingPos,false);
            if(!ignore) GotoUtil.disable();
            ChatUtils.info("Finished trapping " + System.currentTimeMillis());

        });
        waitForTickEventThread1.start();
    }


    private List<BlockPos> getTrap(Entity e) {
        FastBox fastBox = new FastBox(e);
        List<BlockPos> collidedBlocks = fastBox.getOccupiedBlockPos();
        List<BlockPos> trapBlocks = new ArrayList<>();

        int minY = Integer.MAX_VALUE;
        int maxY = Integer.MIN_VALUE;

        for (BlockPos pos : collidedBlocks) {
            minY = Math.min(minY, pos.getY());
            maxY = Math.max(maxY, pos.getY());
        }

        int middleY = minY + (maxY - minY) / 2;

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
