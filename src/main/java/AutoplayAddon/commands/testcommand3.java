package AutoplayAddon.commands;
import AutoplayAddon.utils.*;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import meteordevelopment.meteorclient.commands.Command;
import net.minecraft.block.Blocks;
import net.minecraft.command.CommandSource;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;

public class testcommand3 extends Command {
    private final ItemCollection itemCollection = new ItemCollection();
    public testcommand3() {
        super("randomplace", "finds coordnates of hard wood logs");
    }

    @Override
    public void build(LiteralArgumentBuilder<CommandSource> builder) {
        builder.executes(context -> {
            PlaceUtil.randomplace(Blocks.CRAFTING_TABLE);
            return SINGLE_SUCCESS;
        });
    }
}
