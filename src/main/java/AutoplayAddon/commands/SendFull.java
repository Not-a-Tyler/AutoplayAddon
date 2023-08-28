package AutoplayAddon.commands;

import AutoplayAddon.Mixins.ClientConnectionInvokerMixin;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import meteordevelopment.meteorclient.commands.Command;
import net.minecraft.command.CommandSource;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;
import static meteordevelopment.meteorclient.MeteorClient.mc;

public class SendFull extends Command {

    public SendFull() {
        super("sendfull","Sends a packet to the server with new position. Allows to teleport small distances.");
    }

    @Override
    public void build(LiteralArgumentBuilder<CommandSource> builder) {
        builder.executes(context -> {
            PlayerMoveC2SPacket packet = new PlayerMoveC2SPacket.Full(mc.player.getX(), mc.player.getY(), mc.player.getZ(), mc.player.getYaw(), mc.player.getPitch(), true);
            ((ClientConnectionInvokerMixin) mc.getNetworkHandler().getConnection())._sendImmediately(packet, null);
            return SINGLE_SUCCESS;
        });
    }
}
