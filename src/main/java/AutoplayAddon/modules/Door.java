package AutoplayAddon.modules;
import AutoplayAddon.AutoPlay.Movement.Movement;
import AutoplayAddon.AutoPlay.Other.FastBox;
import AutoplayAddon.AutoplayAddon;
import meteordevelopment.meteorclient.settings.KeybindSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.misc.Keybind;
import meteordevelopment.meteorclient.utils.player.ChatUtils;

public class Door extends Module {
    public Door() {
        super(AutoplayAddon.autoplay, "door", "Example");
    }

    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final Setting<Keybind> Door = sgGeneral.add(new KeybindSetting.Builder()
        .name("keybind-to-door")
        .description("doors when you press this button")
        .defaultValue(Keybind.none())
        .action(() -> {
            FastBox fastbox = new FastBox(mc.player.getPos());
            Boolean hasCollided = false;
            int blockCount = 0; // Add this counter
            while (true) {
                fastbox.offsetInLookDirection(0.5);
                blockCount++; // Increment the counter with each loop iteration

                // Break out of the loop if the block limit is reached
                if (blockCount > 400) {
                    ChatUtils.error("Failed to find a door within 200 blocks!");
                    break;
                }

                if (!fastbox.isCollidingWithBlocks()) {
                    if (hasCollided) {
                        ChatUtils.info("Teleporting");
                        Movement.moveTo(fastbox.position);
                        break;
                    }
                } else {
                    hasCollided = true;
                }
            }
        })
        .build()
    );
}
