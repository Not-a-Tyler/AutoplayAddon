package AutoplayAddon.commands;
import AutoplayAddon.utils.*;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import meteordevelopment.meteorclient.commands.Command;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.command.CommandSource;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;


import java.util.Arrays;
import java.util.List;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;

public class testcommand5 extends Command {
    public testcommand5() {
        super("testcommand5", "Command for testing purposes.");
    }

    @Override
    public void build(LiteralArgumentBuilder<CommandSource> builder) {
        builder.executes(context -> {
            List<Item> targetBlocks = Arrays.asList(Items.IRON_ORE);
            List<Vec3d> collectableBlock = CanPickUpTest.findCollectableItem(targetBlocks, 100);
            if (collectableBlock != null) {
                Vec3d currentPos = collectableBlock.get(0);
                Vec3d airGapPos = collectableBlock.get(1);
                info("Found block at " + currentPos.toString());
                info("Found air gap at " + airGapPos.toString());
            } else {
                info("No target blocks found within the search radius.");
            }
            return SINGLE_SUCCESS;
        });
    }

}
