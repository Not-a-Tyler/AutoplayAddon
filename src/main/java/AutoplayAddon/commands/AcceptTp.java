package AutoplayAddon.commands;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import meteordevelopment.meteorclient.commands.Command;
import net.minecraft.command.CommandSource;
import net.minecraft.network.packet.c2s.play.TeleportConfirmC2SPacket;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;
import static meteordevelopment.meteorclient.MeteorClient.mc;

public class AcceptTp extends Command {

    public AcceptTp() {
        super("accepttp","Sends a packet to the server with new position. Allows to teleport small distances.");
    }

    @Override
    public void build(LiteralArgumentBuilder<CommandSource> builder) {
        builder.then(argument("teleportid", IntegerArgumentType.integer()).executes(context -> {
            Integer id = context.getArgument("teleportid", Integer.class);
            TeleportConfirmC2SPacket packet = new TeleportConfirmC2SPacket(id);
            mc.player.networkHandler.sendPacket(packet);
            return SINGLE_SUCCESS;
        }));
    }
}
