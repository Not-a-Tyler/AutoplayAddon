package AutoplayAddon.commands;

import AutoplayAddon.AutoPlay.Inventory.LogToPlankTest;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import meteordevelopment.meteorclient.commands.Command;
import net.minecraft.command.CommandSource;
import static com.mojang.brigadier.Command.SINGLE_SUCCESS;

public class TestCommand extends Command {
    public TestCommand() {
        super("TestCommand", "Teleports you to the position of your camara, nig");
    }
    @Override
    public void build(LiteralArgumentBuilder<CommandSource> builder) {
        builder.executes(context -> {
            LogToPlankTest.Log2Plank();
            return SINGLE_SUCCESS;
        });
    }
}
