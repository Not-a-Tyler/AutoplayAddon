package AutoplayAddon.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import meteordevelopment.meteorclient.commands.Command;
import net.minecraft.command.CommandSource;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.network.packet.c2s.play.VehicleMoveC2SPacket;
import net.minecraft.client.render.Camera;
import meteordevelopment.meteorclient.utils.player.PlayerUtils;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;
import static meteordevelopment.meteorclient.MeteorClient.mc;

public class TP2cam extends Command {
    public TP2cam() {
        super("tp2cam", "Teleports you to the position of your camara, nig");
    }
    @Override
    public void build(LiteralArgumentBuilder<CommandSource> builder) {
        builder.executes(context -> {
            Camera camera = mc.gameRenderer.getCamera();
            double dist = PlayerUtils.distanceTo(camera.getPos());
            int packetsRequired = (int) Math.ceil(dist / 10.0) - 1;
            info("Distance: " + (dist) + " Bypass Amount: " + packetsRequired);
            info("X: " + (camera.getPos().x)  + "Y: " + (camera.getPos().y) + "Z: " + (camera.getPos().z));
            if (mc.player.hasVehicle()) {
                for (int packetNumber = 0; packetNumber < (packetsRequired); packetNumber++) {
                    mc.player.networkHandler.sendPacket(new VehicleMoveC2SPacket(mc.player.getVehicle()));
                }
                mc.player.getVehicle().setPosition(camera.getPos().x, camera.getPos().y, camera.getPos().z);
                mc.player.networkHandler.sendPacket(new VehicleMoveC2SPacket(mc.player.getVehicle()));
            } else {
                for (int packetNumber = 0; packetNumber < (packetsRequired); packetNumber++) {
                    mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.OnGroundOnly(true));
                }
                mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(camera.getPos().x,camera.getPos().y, camera.getPos().z, true));
                mc.player.setPosition(camera.getPos().x, camera.getPos().y, camera.getPos().z);
            }
            return SINGLE_SUCCESS;
        });
        return;
    }
}
