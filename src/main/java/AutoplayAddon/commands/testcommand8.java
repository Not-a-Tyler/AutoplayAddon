package AutoplayAddon.commands;
import AutoplayAddon.utils.*;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import meteordevelopment.meteorclient.commands.Command;
import net.minecraft.command.CommandSource;
import net.minecraft.item.Items;


import static com.mojang.brigadier.Command.SINGLE_SUCCESS;

public class testcommand8 extends Command {

    public testcommand8() {
        super("CRAFTAFUCKINGPICKAXE", "finds coordinates of raw iron");
    }

    @Override
    public void build(LiteralArgumentBuilder<CommandSource> builder) {
        builder.executes(context -> {
            new Thread(() -> {
                CraftUtil.craftItem(Items.STONE_PICKAXE, 1);
            }).start();
            return SINGLE_SUCCESS;
        });
    }
}
