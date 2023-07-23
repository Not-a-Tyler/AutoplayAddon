package AutoplayAddon.commands;

import AutoplayAddon.AutoPlay.Movement.GotoUtil;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import meteordevelopment.meteorclient.commands.Command;
import net.minecraft.command.CommandSource;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;

public class Stop extends Command {

    public Stop() {
        super("stop","Sends a packet to the server with new position. Allows to teleport small distances.");
    }

    @Override
    public void build(LiteralArgumentBuilder<CommandSource> builder) {
        builder.executes(context -> {
            GotoUtil.stopAllInstances();
            return SINGLE_SUCCESS;
        });
        return;
    }
}
