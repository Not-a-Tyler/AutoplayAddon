package AutoplayAddon.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import meteordevelopment.meteorclient.commands.Command;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import net.minecraft.block.Block;
import net.minecraft.command.CommandSource;
import net.minecraft.command.argument.ItemStackArgumentType;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.util.math.Vec3d;
import AutoplayAddon.AutoPlay.Locator.CanPickUpTest;
import java.util.Collections;
import java.util.List;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;
import static meteordevelopment.meteorclient.MeteorClient.mc;

public class findcollectableblock extends Command {
    public findcollectableblock() {
        super("findcollectableblock", "Mines a Block");
    }

    @Override
    public void build(LiteralArgumentBuilder<CommandSource> builder) {

        // Specific item
        builder.then(argument("item", ItemStackArgumentType.itemStack(REGISTRY_ACCESS)).executes(context -> printItemName(player -> {
            ItemStack stack = ItemStackArgumentType.getItemStackArgument(context, "item").createStack(1, false);

            if (stack == null || stack.getItem() == Items.AIR) ChatUtils.info("e");
            List<Block> blockslist = Collections.singletonList(Block.getBlockFromItem(stack.getItem()));
            ChatUtils.info("testing " + blockslist.get(0).getName().getString());
            ChatUtils.info(String.valueOf(System.currentTimeMillis()));
            List<Vec3d> collectableBlock = CanPickUpTest.findCollectableBlock(blockslist);
            if (collectableBlock == null) {
                ChatUtils.info("Didn't find anything");
                return;
            }
            Vec3d targetpos = collectableBlock.get(0);
            Vec3d airGapPos = collectableBlock.get(1);
            ChatUtils.info("Target: " + targetpos.toString());
            ChatUtils.info("Air Gap: " + airGapPos.toString());
        })));
    }

    private int printItemName(PlayerConsumer consumer) throws CommandSyntaxException {
        consumer.accept(mc.player);
        return SINGLE_SUCCESS;
    }

    // Separate interface so exceptions can be thrown from it (which is not the case for Consumer)
    @FunctionalInterface
    private interface PlayerConsumer {
        void accept(ClientPlayerEntity player) throws CommandSyntaxException;
    }
}
