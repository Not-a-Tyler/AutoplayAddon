package AutoplayAddon.commands;

import AutoplayAddon.AutoPlay.Movement.Movement;
import AutoplayAddon.AutoPlay.Other.ClientPosArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import meteordevelopment.meteorclient.commands.Command;
import net.minecraft.command.CommandSource;
import net.minecraft.util.math.Vec3d;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;

public class Teleport extends Command {

    public Teleport() {
        super("sexteleport","Sends a packet to the server with new position. Allows to teleport small distances.", "tpbypass", "tpb", "tp");
    }

    @Override
    public void build(LiteralArgumentBuilder<CommandSource> builder) {
        builder.then(argument("pos", ClientPosArgumentType.pos()).executes(ctx -> {
            Vec3d pos = ClientPosArgumentType.getPos(ctx, "pos");
            info("pos: " + pos);
            Movement.moveTo(new Vec3d(pos.getX(), pos.getY(), pos.getZ()));
            return SINGLE_SUCCESS;
        }));
    }
}
