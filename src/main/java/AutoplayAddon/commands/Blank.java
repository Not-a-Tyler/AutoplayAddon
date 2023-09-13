package AutoplayAddon.commands;
import AutoplayAddon.AutoPlay.Movement.GotoUtil;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import meteordevelopment.meteorclient.commands.Command;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import net.minecraft.command.CommandSource;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;
import static meteordevelopment.meteorclient.MeteorClient.mc;

public class Blank extends Command {
    public Blank() {
        super("blankmsg", "Teleports you to the position of your camara, nig");
    }
    @Override
    public void build(LiteralArgumentBuilder<CommandSource> builder) {
        builder.executes(context -> {
            ChatUtils.info("sending blank mess1age");
            //mc.player.networkHandler.sendChatMessage("\t");
            //mc.player.networkHandler.sendChatMessage(" ");
            //mc.player.networkHandler.sendChatMessage("\u00AD");
            //mc.player.networkHandler.sendChatMessage("͏");
            return SINGLE_SUCCESS;
        });
    }
}
