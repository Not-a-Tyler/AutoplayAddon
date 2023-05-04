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

public class testcommand extends Command {
    private final SmartMine smartMine = new SmartMine();
    public testcommand() {
        super("ironmine", "mines a rock ");
    }

    @Override
    public void build(LiteralArgumentBuilder<CommandSource> builder) {
        builder.executes(context -> {
            new Thread(() -> {
                List<Item> targetBlocks2 = Arrays.asList(Items.IRON_ORE);
                smartMine.processBlocks(targetBlocks2);
            }).start();
            return SINGLE_SUCCESS;
        });
    }
}
