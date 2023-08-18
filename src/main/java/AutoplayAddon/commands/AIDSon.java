package AutoplayAddon.commands;

import AutoplayAddon.AutoPlay.Movement.AIDS;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import meteordevelopment.meteorclient.commands.Command;
import net.minecraft.command.CommandSource;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.network.packet.c2s.play.VehicleMoveC2SPacket;
import net.minecraft.client.render.Camera;
import meteordevelopment.meteorclient.utils.player.PlayerUtils;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;
import static meteordevelopment.meteorclient.MeteorClient.mc;

public class AIDSon extends Command {
    public AIDSon() {
        super("aidson", "Teleports you to the position of your camara, nig");
    }
    @Override
    public void build(LiteralArgumentBuilder<CommandSource> builder) {
        builder.executes(context -> {
            AIDS.init(true);
            return SINGLE_SUCCESS;
        });
    }
}
