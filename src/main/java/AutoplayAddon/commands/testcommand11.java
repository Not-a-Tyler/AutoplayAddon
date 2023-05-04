package AutoplayAddon.commands;

import AutoplayAddon.utils.CookingUtils;
import AutoplayAddon.utils.CraftUtil;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import meteordevelopment.meteorclient.commands.Command;
import net.minecraft.command.CommandSource;
import net.minecraft.item.Items;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;

public class testcommand11 extends Command {

    public testcommand11() {
        super("cookdairon", "finds coordinates of raw iron");
    }

    @Override
    public void build(LiteralArgumentBuilder<CommandSource> builder) {
        builder.executes(context -> {
            new Thread(() -> {
                CookingUtils.cook(Items.RAW_IRON);
            }).start();
            return SINGLE_SUCCESS;
        });
    }
}
