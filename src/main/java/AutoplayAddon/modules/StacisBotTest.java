package AutoplayAddon.modules;
import AutoplayAddon.AutoplayAddon;
import AutoplayAddon.BotTest.Commands.FollowCommand;
import AutoplayAddon.BotTest.Commands.SneakCommand;
import AutoplayAddon.BotTest.Commands.TwerkCommand;
import AutoplayAddon.Mixins.ClientCommandC2SPacketMixin;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.events.game.ReceiveMessageEvent;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.orbit.EventPriority;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StacisBotTest extends Module {

    public StacisBotTest() {
        super(AutoplayAddon.autoplay, "tyler-the-bot", "Used to join the ultimate botnet.");
    }
    SneakCommand sneakCommand = new SneakCommand();
    FollowCommand followCommand = new FollowCommand();
    TwerkCommand twerkCommand = new TwerkCommand();

    @EventHandler
    private void onMessageReceive(ReceiveMessageEvent event) {
        assert mc.player != null;
        String msg = event.getMessage().getString();
        // normal message pattern
        Pattern playerNamePattern = Pattern.compile("<([^|>]+)");
        // /me pattern
        Pattern playerNameMePattern = Pattern.compile("^\\*\\s([^\\s]+)\\s+(.*)$");
        String playerName;
        String message;
        Matcher matcher = playerNamePattern.matcher(msg);
        Matcher meMatcher = playerNameMePattern.matcher(msg);

        if (matcher.find()) {
            playerName = matcher.group(1).trim(); // Trim to remove leading/trailing spaces
            Matcher messageMatcher = Pattern.compile(">\\s*(.*)").matcher(msg);
            if (messageMatcher.find()) {
                message = messageMatcher.group(1);
            } else {
                message = "";
            }
        } else if (meMatcher.find()) {
            playerName = meMatcher.group(1).trim(); // Trim to remove leading/trailing spaces
            message = meMatcher.group(2);
        } else {
            System.out.println("No match found");
            return;
        }

        if (playerName.equals(mc.player.getDisplayName().getString().trim())) return;

        try {
            CommandProcessor commandProcessor = new CommandProcessor();
            commandProcessor.processCommand(playerName, message);
        } catch (IllegalArgumentException e) {
            System.out.println(e.getMessage());
        }
    }

    public static boolean sneak = false;

    @EventHandler(priority = EventPriority.HIGHEST + 1)
    private void onSendPacket(PacketEvent.Send event) {
        if (event.packet instanceof ClientCommandC2SPacket packet) {
            ClientCommandC2SPacketMixin accessor = (ClientCommandC2SPacketMixin) event.packet;
            if(packet.getMode() == ClientCommandC2SPacket.Mode.PRESS_SHIFT_KEY && accessor.getMountJumpHeight() != 2367) {
                event.cancel();
            }
            if(packet.getMode() == ClientCommandC2SPacket.Mode.RELEASE_SHIFT_KEY && accessor.getMountJumpHeight() != 2367) {
                event.cancel();
            }
        }
    }


    public class CommandProcessor {

        public void processCommand(String playerName, String command) {
            String[] parts = command.split(" ", 2);

            switch (parts[0].toLowerCase()) {
                case "!follow" -> {
                    MeteorClient.EVENT_BUS.subscribe(followCommand);
                    followCommand.processFollowCommand(playerName, parts.length > 1 ? parts[1] : null);
                }
                case "!stop" -> {
                    //GotoUtil.stopAllInstances();
                    TwerkCommand.twerk = false;
                    if (sneak) {
                        mc.player.networkHandler.sendPacket(new ClientCommandC2SPacket(mc.player, ClientCommandC2SPacket.Mode.RELEASE_SHIFT_KEY, 2367));
                        sneak = false;
                    }
                }
                case "!sneak" -> {
                    MeteorClient.EVENT_BUS.subscribe(sneakCommand);
                    sneakCommand.processSneakCommand(playerName, parts.length > 1 ? parts[1] : null);
                }
                case "!twerk" -> {
                    MeteorClient.EVENT_BUS.subscribe(twerkCommand);
                    twerkCommand.processSneakCommand(playerName, parts.length > 1 ? parts[1] : null);
                }
                default -> throw new IllegalArgumentException("Invalid command: " + parts[0]);
            }
        }
    }

}
