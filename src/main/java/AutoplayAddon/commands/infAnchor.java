package AutoplayAddon.commands;

import AutoplayAddon.AutoPlay.Movement.GotoUtil;
import AutoplayAddon.AutoPlay.Other.FastBox;
import AutoplayAddon.AutoPlay.Other.Packet;
import AutoplayAddon.Mixins.ClientConnectionInvokerMixin;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import meteordevelopment.meteorclient.commands.Command;
import meteordevelopment.meteorclient.commands.arguments.PlayerArgumentType;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import net.minecraft.command.CommandSource;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import static com.mojang.brigadier.Command.SINGLE_SUCCESS;
import static meteordevelopment.meteorclient.MeteorClient.mc;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;

import java.util.ArrayList;
import java.util.List;

public class infAnchor extends Command {
    public infAnchor() {
        super("infanchor", "kill anyone anytime anywhere real");
    }
    private void switchToHotbar(Item targetItem) {
        for (int i = 0; i < 9; i++) {
            if (mc.player.getInventory().getStack(i).getItem() == targetItem) {
                Packet.sendPacket(new UpdateSelectedSlotC2SPacket(i));
            }
        }
    }

    private void switchToEmptyHotbarSlot() {
        for (int i = 0; i < 9; i++) {
            if (mc.player.getInventory().getStack(i).isEmpty() && mc.player.getInventory().getStack(i).getItem() != Items.GLOWSTONE) {
                Packet.sendPacket(new UpdateSelectedSlotC2SPacket(i));
                break;
            }
        }
    }

    @Override
    public void build(LiteralArgumentBuilder<CommandSource> builder) {
        builder.then(argument("player", PlayerArgumentType.create()).executes(context -> {
            PlayerEntity e = PlayerArgumentType.get(context);
            Vec3d startingPos = mc.player.getPos();
            Thread waitForTickEventThread1 = new Thread(() -> {
                ChatUtils.info("hitting " + e.getName());
                GotoUtil.init(false, true);
                BlockPos finalBlockPos = getAnchorPos(e);
                GotoUtil.setPos(e.getPos(), true);
                ChatUtils.info("Trapping " + e.getName().getString() + " at " + finalBlockPos);
                switchToHotbar(Items.RESPAWN_ANCHOR);
                Packet.sendPacket(new PlayerInteractBlockC2SPacket(Hand.MAIN_HAND, new BlockHitResult(finalBlockPos.toCenterPos(), Direction.EAST, finalBlockPos, false), 0));
                switchToHotbar(Items.GLOWSTONE);
                Packet.sendPacket(new PlayerInteractBlockC2SPacket(Hand.MAIN_HAND, new BlockHitResult(finalBlockPos.toCenterPos(), Direction.EAST, finalBlockPos, false), 0));
                switchToEmptyHotbarSlot();
                Packet.sendPacket(new PlayerInteractBlockC2SPacket(Hand.MAIN_HAND, new BlockHitResult(finalBlockPos.toCenterPos(), Direction.EAST, finalBlockPos, false), 0));
                GotoUtil.setPos(startingPos, false);
                GotoUtil.disable();
            });
            waitForTickEventThread1.start();
            return SINGLE_SUCCESS;
        }));
    }

    private BlockPos getAnchorPos(Entity e) {
        FastBox fastBox = new FastBox(e);
        List<BlockPos> collidedBlocks = fastBox.getOccupiedBlockPos();
        List<BlockPos> trapBlocks = new ArrayList<>();

        int minX = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE;
        int minY = Integer.MAX_VALUE;
        int maxY = Integer.MIN_VALUE;
        int minZ = Integer.MAX_VALUE;
        int maxZ = Integer.MIN_VALUE;

        for (BlockPos pos : collidedBlocks) {
            minX = Math.min(minX, pos.getX());
            maxX = Math.max(maxX, pos.getX());
            minY = Math.min(minY, pos.getY());
            maxY = Math.max(maxY, pos.getY());
            minZ = Math.min(minZ, pos.getZ());
            maxZ = Math.max(maxZ, pos.getZ());
        }

        for (BlockPos blockPos : collidedBlocks) {
            for (Direction dir : Direction.values()) {
                BlockPos offsetPos = blockPos.offset(dir);
                if (!collidedBlocks.contains(offsetPos)) {
                    trapBlocks.add(offsetPos);
                }
            }
        }

        // Cover the top and bottom extremes
        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                trapBlocks.add(new BlockPos(x, minY - 1, z));
                trapBlocks.add(new BlockPos(x, maxY + 1, z));
            }
        }
        double farthestDistance = 0;
        BlockPos farthestBlock = null;
        for (BlockPos block : trapBlocks) {
            if (mc.world.getBlockState(block).isSolid()) continue;
            double distance = block.toCenterPos().distanceTo(e.getPos());
            if (distance < farthestDistance) {
                farthestDistance = distance;
                farthestBlock = block;
            }
        }
        return farthestBlock;
    }



}
