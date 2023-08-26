package AutoplayAddon.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import meteordevelopment.meteorclient.commands.Command;
import net.minecraft.command.CommandSource;

import static meteordevelopment.meteorclient.MeteorClient.mc;

public class disablegravity extends Command {

    public disablegravity() {
        super("disablegravity","Sends a packet to the server with new position. Allows to teleport small distances.", "tpbypass", "tpb", "tp");
    }

    @Override
    public void build(LiteralArgumentBuilder<CommandSource> builder) {
        if (mc.player != null) {
            mc.player.setNoGravity(true);
        }
    }
}

