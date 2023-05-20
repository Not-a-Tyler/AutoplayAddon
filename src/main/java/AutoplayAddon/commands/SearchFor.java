package AutoplayAddon.commands;
import AutoplayAddon.utils.GetLocUtil;
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
import net.minecraft.util.math.BlockPos;

import java.util.Arrays;
import java.util.List;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;
import static meteordevelopment.meteorclient.MeteorClient.mc;

public class SearchFor extends Command {
    public SearchFor() {
        super("searchfor", "Mines a Block");
    }

    @Override
    public void build(LiteralArgumentBuilder<CommandSource> builder) {
        // Specific item
        builder.then(argument("item", ItemStackArgumentType.itemStack(REGISTRY_ACCESS)).executes(context -> printItemName(player -> {
            ItemStack stack = ItemStackArgumentType.getItemStackArgument(context, "item").createStack(1, false);

            if (stack == null || stack.getItem() == Items.AIR) ChatUtils.info("e");
            ChatUtils.info(stack.getItem().getName().getString());
            List<Block> itemslist = Arrays.asList(Block.getBlockFromItem(stack.getItem()));

            long startTime = System.currentTimeMillis();

            new Thread(() -> {
                BlockPos pos = GetLocUtil.findBlocks(itemslist, 200);
                long endTime = System.currentTimeMillis();
                long executionTime = endTime - startTime;
                ChatUtils.info("Thread execution time: " + executionTime + " milliseconds " + pos.toShortString());
            }).start();
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
