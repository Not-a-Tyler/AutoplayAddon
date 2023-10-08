package AutoplayAddon.commands;

import AutoplayAddon.AutoPlay.Locator.CanPickUpTest;
import AutoplayAddon.AutoPlay.Movement.GotoUtil;
import AutoplayAddon.AutoPlay.Other.Packet;
import AutoplayAddon.AutoplayAddon;
import AutoplayAddon.Mixins.ClientConnectionInvokerMixin;
import AutoplayAddon.Tracker.ServerSideValues;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import meteordevelopment.meteorclient.commands.Command;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.command.CommandSource;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

import java.util.Collections;
import java.util.List;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;
import static meteordevelopment.meteorclient.MeteorClient.mc;

public class SendFull extends Command {

    public SendFull() {
        super("sendfull","Sends a packet to the server with new position. Allows to teleport small distances.");
    }
    int BlocksBroken = 0;
    @Override
    public void build(LiteralArgumentBuilder<CommandSource> builder) {
        builder.executes(context -> {
            Direction direction = Direction.UP;
            int blockrange = 3;
            List<Item> targetBlocks6 = Collections.singletonList(Items.COBBLESTONE);
            List<Vec3d> collectableBlock = CanPickUpTest.findCollectableItem(targetBlocks6);
            if (collectableBlock == null) {
                ChatUtils.info("No blocks found");
                return SINGLE_SUCCESS;
            }
            Vec3d airGapPos = collectableBlock.get(1);
            BlockPos blockPos = new BlockPos((int) airGapPos.x, (int) airGapPos.y, (int) airGapPos.z);
            //if (!GotoUtil.setPos(airGapPos, false)) return SINGLE_SUCCESS;

            for (int x = -blockrange; x <= blockrange; x++) {
                for (int y = -blockrange; y <= blockrange; y++) {
                    for (int z = -blockrange; z <= blockrange; z++) {
                        // Calculate the position of the block we're checking
                        BlockPos currentBlockPos = new BlockPos(blockPos.getX() + x, blockPos.getY() + y, blockPos.getZ() + z);
                        if (!ServerSideValues.canSendPackets(2, System.nanoTime())) {
                            ChatUtils.info("hit limit Blocks broken: " + BlocksBroken);
                            BlocksBroken = 0;
                            return SINGLE_SUCCESS;
                        }
                        if (AutoplayAddon.blockCache.isBlockAt(currentBlockPos)) {
                            BlocksBroken++;
                            Packet.sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.START_DESTROY_BLOCK, currentBlockPos, direction));
                            Packet.sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, currentBlockPos, direction));
                            AutoplayAddon.blockCache.removeBlock(currentBlockPos);
                            mc.world.setBlockState(currentBlockPos, Blocks.AIR.getDefaultState());
                        }
                    }
                }
            }
            ChatUtils.info("blocks broken: " + BlocksBroken);
            BlocksBroken = 0;
            return SINGLE_SUCCESS;
        });
    }

}
