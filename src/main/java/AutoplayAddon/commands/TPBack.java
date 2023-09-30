package AutoplayAddon.commands;

import AutoplayAddon.AutoPlay.Movement.GotoUtil;
import AutoplayAddon.AutoPlay.Movement.Movement;
import AutoplayAddon.AutoPlay.Other.ClientPosArgumentType;
import AutoplayAddon.Tracker.ServerSideValues;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import meteordevelopment.meteorclient.commands.Command;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import net.minecraft.command.CommandSource;
import net.minecraft.util.math.Vec3d;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;
import static meteordevelopment.meteorclient.MeteorClient.mc;

public class TPBack extends Command {

    public TPBack() {
        super("tpback","Sends a packet to the server with new position. Allows to teleport small distances.", "tpbypass", "tpb", "tp");
    }

    @Override
    public void build(LiteralArgumentBuilder<CommandSource> builder) {
        builder.then(argument("pos", ClientPosArgumentType.pos()).executes(ctx -> {
            Vec3d pos = ClientPosArgumentType.getPos(ctx, "pos");
            new Thread(() -> {
                Vec3d startingPos = mc.player.getPos();
                boolean ignore;
                if (Movement.AIDSboolean) {
                    ignore = false;
                } else {
                    ignore = true;
                }
                if (ignore) GotoUtil.init(false, true);
                GotoUtil.setPos(pos, true);
                ChatUtils.info("time: " + System.currentTimeMillis() + " i2 between: " + ServerSideValues.i2);
                GotoUtil.setPos(startingPos, false);
                if (ignore) GotoUtil.disable();
                ChatUtils.info("time after: " + System.currentTimeMillis());
            }).start();
            return SINGLE_SUCCESS;
        }));
    }
}
