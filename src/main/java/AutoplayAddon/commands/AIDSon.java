package AutoplayAddon.commands;

import AutoplayAddon.AutoPlay.Movement.GotoUtil;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import meteordevelopment.meteorclient.commands.Command;
import net.minecraft.command.CommandSource;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;

public class AIDSon extends Command {
    public AIDSon() {
        super("aidson", "Teleports you to the position of your camara, nig");
    }
    @Override
    public void build(LiteralArgumentBuilder<CommandSource> builder) {
        builder.executes(context -> {
            GotoUtil.init();
            return SINGLE_SUCCESS;
        });
    }
}
