package AutoplayAddon.commands;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import meteordevelopment.meteorclient.utils.player.ChatUtils;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import meteordevelopment.meteorclient.commands.Command;
import net.minecraft.command.CommandSource;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;

import java.lang.reflect.Field;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;
import static meteordevelopment.meteorclient.MeteorClient.mc;

public class Smack extends Command {

    public Smack() {
        super("smack2","Sends a packet to the server with new position. Allows to teleport small distances.");
    }

    @Override
    public void build(LiteralArgumentBuilder<CommandSource> builder) {
        builder.then(argument("entityid", IntegerArgumentType.integer()).executes(context -> {
            Integer id = context.getArgument("entityid", Integer.class);
            ChatUtils.info("attempting to smack " + id);
            PlayerInteractEntityC2SPacket packet1 = PlayerInteractEntityC2SPacket.attack(mc.player, false);
            try {
                Field entityIdField = PlayerInteractEntityC2SPacket.class.getDeclaredField("entityId");
                entityIdField.setAccessible(true);
                entityIdField.set(packet1, id); // Change this from 'packet' to 'packet1'
            } catch (NoSuchFieldException | IllegalAccessException e) {
                e.printStackTrace();
            }
            mc.player.networkHandler.sendPacket(packet1);
            return SINGLE_SUCCESS;
        }));
    }
}
