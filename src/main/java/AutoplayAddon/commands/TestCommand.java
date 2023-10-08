package AutoplayAddon.commands;

import AutoplayAddon.AutoPlay.Inventory.LogToPlankTest;
import AutoplayAddon.AutoPlay.Movement.GotoUtil;
import AutoplayAddon.AutoPlay.Movement.Movement;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import meteordevelopment.meteorclient.commands.Command;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import net.minecraft.command.CommandSource;
import static com.mojang.brigadier.Command.SINGLE_SUCCESS;
import static meteordevelopment.meteorclient.MeteorClient.mc;

public class TestCommand extends Command {
    public TestCommand() {
        super("TestCommand", "Teleports you to the position of your camara, nig");
    }
    @Override
    public void build(LiteralArgumentBuilder<CommandSource> builder) {
        builder.executes(context -> {
            ChatUtils.sendPlayerMsg("Starting");
            ChatUtils.info("starting test command");
            new Thread(() -> {
                GotoUtil.init(true, true);
                GotoUtil.setPos(Movement.currentPosition.add(0, 200, 0), false);
                GotoUtil.setPos(Movement.currentPosition.add(0, 200, 0), false);
                GotoUtil.setPos(Movement.currentPosition.add(0, 200, 0), false);
                ChatUtils.info("test command finished");
            }).start();
            return SINGLE_SUCCESS;
        });
    }
}
