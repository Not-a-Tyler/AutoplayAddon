package AutoplayAddon.commands;

import AutoplayAddon.AutoPlay.Inventory.LogToPlankTest;
import AutoplayAddon.AutoPlay.Movement.GotoUtil;
import AutoplayAddon.AutoPlay.Movement.Movement;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import meteordevelopment.meteorclient.commands.Command;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import net.minecraft.command.CommandSource;
import net.minecraft.util.math.Vec3d;

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
                GotoUtil.init();
                Vec3d pos = Movement.currentPosition;
                GotoUtil.setPos(pos.add(0, 200, 0), false, true, false);
                GotoUtil.setPos(pos.add(0, 400, 0), false, true, false);
                GotoUtil.setPos(pos.add(0, 600, 0), false, true, true);
                ChatUtils.info("test command finished");
            }).start();
            return SINGLE_SUCCESS;
        });
    }
}
