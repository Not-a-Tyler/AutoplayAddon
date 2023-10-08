package AutoplayAddon.commands;

import AutoplayAddon.AutoPlay.Movement.GotoUtil;
import AutoplayAddon.AutoPlay.Movement.Movement;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import meteordevelopment.meteorclient.commands.Command;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import net.minecraft.command.CommandSource;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;
import static meteordevelopment.meteorclient.MeteorClient.mc;

public class Craw extends Command {
    public Craw() {
        super("craw", "Teleports you to the position of your camara, nig");
    }
    @Override
    public void build(LiteralArgumentBuilder<CommandSource> builder) {
        builder.executes(context -> {
            BlockPos topBlock;
            BlockPos playerBlockPos = mc.player.getBlockPos();
            int num = 0;
            while (true) {
                num++;
                BlockPos block = new BlockPos(playerBlockPos.getX(), (playerBlockPos.getY() + num), playerBlockPos.getZ());
                if (mc.world.getBlockState(block).isSolid()) {
                    topBlock = block;
                    break;
                }
            }
            ChatUtils.info("found solid block at " + topBlock);
            mc.player.networkHandler.sendPacket(new ClientCommandC2SPacket(mc.player, ClientCommandC2SPacket.Mode.PRESS_SHIFT_KEY));
            Vec3d tpPos = new Vec3d(mc.player.getX(), (topBlock.getY() - 1.5), mc.player.getZ());
            ChatUtils.info("teleporting to " + tpPos);
            mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.Full(tpPos.x, tpPos.y, tpPos.z, mc.player.getYaw(), mc.player.getPitch(), true));
            mc.player.networkHandler.sendPacket(new ClientCommandC2SPacket(mc.player, ClientCommandC2SPacket.Mode.RELEASE_SHIFT_KEY));
            return SINGLE_SUCCESS;
        });
    }
}
