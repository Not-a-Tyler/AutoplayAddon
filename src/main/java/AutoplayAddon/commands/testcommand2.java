package AutoplayAddon.commands;
import AutoplayAddon.utils.*;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import meteordevelopment.meteorclient.commands.Command;
import net.minecraft.command.CommandSource;
import net.minecraft.item.Item;
import net.minecraft.item.Items;

import java.util.Arrays;
import java.util.List;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;

public class testcommand2 extends Command {
    private final ItemCollection itemCollection = new ItemCollection();

    public testcommand2() {
        super("ironcollect", "finds coordinates of raw iron");
    }

    @Override
    public void build(LiteralArgumentBuilder<CommandSource> builder) {
        builder.executes(context -> {
            new Thread(() -> {
                List<Item> targetItems = Arrays.asList(Items.RAW_IRON);
                itemCollection.collect(targetItems);
            }).start();
            return SINGLE_SUCCESS;
        });
    }
}
