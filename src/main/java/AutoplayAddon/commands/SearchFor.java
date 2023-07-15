package AutoplayAddon.commands;

import AutoplayAddon.AutoplayAddon;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import meteordevelopment.meteorclient.commands.Command;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import net.minecraft.command.CommandSource;
import net.minecraft.command.argument.ItemStackArgumentType;
import net.minecraft.item.ItemStack;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.block.Block;
import net.minecraft.util.math.BlockPos;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;
import static meteordevelopment.meteorclient.MeteorClient.mc;

public class SearchFor extends Command {
    public SearchFor() {
        super("searchfor", "attempts to locate a block");
    }

    @Override
    public void build(LiteralArgumentBuilder<CommandSource> builder) {
        builder.then(argument("item", ItemStackArgumentType.itemStack(REGISTRY_ACCESS)).executes(context -> printItemName(player -> {
            // Get the current time in milliseconds
            ItemStack stack = ItemStackArgumentType.getItemStackArgument(context, "item").createStack(1, false);
            Block block = Block.getBlockFromItem(stack.getItem());
            ChatUtils.info("Looking for " + block.getName().getString());
            long startTime = System.currentTimeMillis();
            BlockPos bruz = AutoplayAddon.blockCache.getNearestBlock(block);
            long endTime = System.currentTimeMillis(); // Get the current time after calculating BlockPos
            long timeTaken = endTime - startTime; // Calculate the time taken in milliseconds
            ChatUtils.info("Time taken: " + timeTaken + " milliseconds");
            if (bruz != null) {
                ChatUtils.info(bruz.toShortString());
            } else {
                ChatUtils.info("Didn't find anything");
            }
        })));
        return;
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
