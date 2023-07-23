package AutoplayAddon.BotTest.Commands;

import AutoplayAddon.BotTest.ArgumentType;
import AutoplayAddon.modules.StacisBotTest;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;

import static meteordevelopment.meteorclient.MeteorClient.mc;

public class TwerkCommand {
    public static boolean twerk = false;
    public static boolean twerked = false;
    private ArgumentType.BooleanType booleanArgumentType = new ArgumentType.BooleanType();
    public void processSneakCommand(String playerName, String argument) {
        if (argument == null || booleanArgumentType.parse(argument)) {
            twerk = true;
            ChatUtils.sendPlayerMsg("Twerking enabled.");
        } else if (booleanArgumentType.parse(argument) == false) {
            mc.player.networkHandler.sendPacket(new ClientCommandC2SPacket(mc.player, ClientCommandC2SPacket.Mode.RELEASE_SHIFT_KEY, 2367));
            twerk = false;
            ChatUtils.sendPlayerMsg("Twerking disabled.");
        }
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (!twerk) return;
        if(twerked){
            mc.player.networkHandler.sendPacket(new ClientCommandC2SPacket(mc.player, ClientCommandC2SPacket.Mode.RELEASE_SHIFT_KEY, 2367));
            StacisBotTest.sneak = false;
            twerked = false;
        } else {
            mc.player.networkHandler.sendPacket(new ClientCommandC2SPacket(mc.player, ClientCommandC2SPacket.Mode.PRESS_SHIFT_KEY, 2367));
            StacisBotTest.sneak = true;
            twerked = true;
        }

    }
}
