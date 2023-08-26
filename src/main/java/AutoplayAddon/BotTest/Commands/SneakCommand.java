package AutoplayAddon.BotTest.Commands;
import AutoplayAddon.BotTest.ArgumentType;
import AutoplayAddon.modules.StacisBotTest;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;

import static meteordevelopment.meteorclient.MeteorClient.mc;

public class SneakCommand {
    private final ArgumentType.BooleanType booleanArgumentType = new ArgumentType.BooleanType();

    public void processSneakCommand(String playerName, String argument) {
        if (argument == null || booleanArgumentType.parse(argument)) {
            StacisBotTest.sneak = true;
            TwerkCommand.twerk = false;
            mc.player.networkHandler.sendPacket(new ClientCommandC2SPacket(mc.player, ClientCommandC2SPacket.Mode.PRESS_SHIFT_KEY, 2367));
            ChatUtils.sendPlayerMsg("Sneaking enabled.");
        } else if (!booleanArgumentType.parse(argument)) {
            mc.player.networkHandler.sendPacket(new ClientCommandC2SPacket(mc.player, ClientCommandC2SPacket.Mode.RELEASE_SHIFT_KEY, 2367));
            TwerkCommand.twerk = false;
            StacisBotTest.sneak = false;
            ChatUtils.sendPlayerMsg("Sneaking disabled.");
        }
    }
}
