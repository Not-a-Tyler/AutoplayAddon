package AutoplayAddon.commands;
import AutoplayAddon.utils.*;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import meteordevelopment.meteorclient.commands.Command;
import net.minecraft.block.Blocks;
import net.minecraft.command.CommandSource;
import net.minecraft.item.Items;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;

public class testcommand4 extends Command {
    public testcommand4() {
        super("testcommand4", "Command for testing purposes.");
    }

    @Override
    public void build(LiteralArgumentBuilder<CommandSource> builder) {
        builder.executes(context -> {
            CraftUtil.craftItem(Items.OAK_PLANKS, 1);
            CraftUtil.craftItem(Items.CRAFTING_TABLE, 1);
            new Thread(() -> {
                try {
                    Thread.sleep(1000); // Add a delay of 5 seconds
                    PlaceUtil.randomplace(Blocks.CRAFTING_TABLE);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }).start();
            return SINGLE_SUCCESS;
        });
    }

}
