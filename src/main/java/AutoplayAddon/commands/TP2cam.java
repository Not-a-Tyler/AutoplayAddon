package AutoplayAddon.commands;

import AutoplayAddon.AutoPlay.Movement.Movement;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import meteordevelopment.meteorclient.commands.Command;
import net.minecraft.command.CommandSource;
import net.minecraft.util.math.Vec3d;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;
import static meteordevelopment.meteorclient.MeteorClient.mc;

public class TP2cam extends Command {
    public TP2cam() {
        super("tp2cam", "Teleports you to the position of your camara, nig");
    }
    @Override
    public void build(LiteralArgumentBuilder<CommandSource> builder) {
        builder.executes(context -> {
            Vec3d cameraPos = mc.gameRenderer.getCamera().getPos();
            Vec3d playerPos = new Vec3d(cameraPos.x, cameraPos.y - mc.player.getEyeHeight(mc.player.getPose()), cameraPos.z);
            Movement.moveTo(playerPos);
            return SINGLE_SUCCESS;
        });
    }
}
