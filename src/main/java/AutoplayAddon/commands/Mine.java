package AutoplayAddon.commands;

import AutoplayAddon.AutoPlay.Controller.SmartMine;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import meteordevelopment.meteorclient.commands.Command;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import net.minecraft.command.CommandSource;
import net.minecraft.command.argument.ItemStackArgumentType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.client.network.ClientPlayerEntity;

import java.util.Collections;
import java.util.List;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;
import static meteordevelopment.meteorclient.MeteorClient.mc;

public class Mine extends Command {
    public Mine() {
        super("mine", "Mines a Block");
    }

    @Override
    public void build(LiteralArgumentBuilder<CommandSource> builder) {

        // Specific item
        builder.then(argument("item", ItemStackArgumentType.itemStack(REGISTRY_ACCESS)).executes(context -> printItemName(player -> {
            ItemStack stack = ItemStackArgumentType.getItemStackArgument(context, "item").createStack(1, false);
            if (stack == null || stack.getItem() == Items.AIR) ChatUtils.error("Invalid item");
            ChatUtils.info("Attempting to mine " + stack.getItem().getName().getString());
            List<Item> itemslist = Collections.singletonList(stack.getItem());
            new Thread(() -> {
                SmartMine.mineBlocks(itemslist);
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
