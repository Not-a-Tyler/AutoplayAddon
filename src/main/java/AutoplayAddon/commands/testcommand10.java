package AutoplayAddon.commands;
import AutoplayAddon.utils.*;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import meteordevelopment.meteorclient.commands.Command;
import net.minecraft.command.CommandSource;
import net.minecraft.item.Items;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;

public class testcommand10 extends Command {

    public testcommand10() {
        super("CRAFTAFUCKINGPAPER", "finds coordinates of raw iron");
    }

    @Override
    public void build(LiteralArgumentBuilder<CommandSource> builder) {
        builder.executes(context -> {
            new Thread(() -> {
                CraftUtil.craftItem(Items.PAPER, 1);
            }).start();
            return SINGLE_SUCCESS;
        });
    }
}
