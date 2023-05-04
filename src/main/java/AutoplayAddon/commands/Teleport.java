package AutoplayAddon.commands;
import AutoplayAddon.utils.*;
import meteordevelopment.meteorclient.MeteorClient;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import meteordevelopment.meteorclient.commands.Command;
import net.minecraft.command.CommandSource;
import net.minecraft.util.math.Vec3d;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;

public class Teleport extends Command {

    private final GotoUtil gotoUtil = new GotoUtil();
    public Teleport() {
        super("sexteleport","Sends a packet to the server with new position. Allows to teleport small distances.", "tpbypass", "tpb", "tp");
    }

    @Override
    public void build(LiteralArgumentBuilder<CommandSource> builder) {
        builder.then(argument("pos", ClientPosArgumentType.pos()).executes(ctx -> {
            Thread waitForTickEventThread = new Thread(() -> {
                Vec3d pos = ClientPosArgumentType.getPos(ctx, "pos");
                info("pos: " + pos);
                MeteorClient.EVENT_BUS.subscribe(gotoUtil);
                gotoUtil.moveto(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
                MeteorClient.EVENT_BUS.unsubscribe(gotoUtil);
            });
            waitForTickEventThread.start();
            return SINGLE_SUCCESS;
        }));
    }
}
