package AutoplayAddon.commands;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import meteordevelopment.meteorclient.commands.arguments.PlayerArgumentType;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import net.minecraft.command.CommandSource;
import net.minecraft.entity.player.PlayerEntity;
import meteordevelopment.meteorclient.commands.Command;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.network.packet.c2s.play.TeleportConfirmC2SPacket;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;
import static meteordevelopment.meteorclient.MeteorClient.mc;


public class Getid extends Command {
    public Getid() {
        super("getid", "ban");
    }
    @Override
    public void build(LiteralArgumentBuilder<CommandSource> builder) {
        builder.then(argument("player", PlayerArgumentType.create()).executes(context -> {
            int blocks = 190;
            int packetsRequired = (int) Math.ceil(blocks / 10) - 1;


            for (int packetNumber = 0; packetNumber < (packetsRequired - 1); packetNumber++) {
                mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.OnGroundOnly(true));
            }
            mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(mc.player.getX(), mc.player.getY() + blocks, mc.player.getZ(), true));
            mc.player.setPosition(mc.player.getX(), mc.player.getY() + blocks, mc.player.getZ());
            mc.player.networkHandler.sendPacket(new TeleportConfirmC2SPacket(0));
            PlayerEntity e = PlayerArgumentType.get(context);
            ChatUtils.info("player id is " + e.getId() + " width " + mc.player.getWidth() + " height " + mc.player.getHeight());
            return SINGLE_SUCCESS;
        }));
    }
}
