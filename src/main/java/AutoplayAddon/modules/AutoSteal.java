package AutoplayAddon.modules;

import AutoplayAddon.AutoPlay.Actions.ItemCollection;
import AutoplayAddon.AutoPlay.Movement.AIDS;
import AutoplayAddon.AutoPlay.Movement.GotoUtil;
import AutoplayAddon.AutoplayAddon;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.ItemListSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.item.Item;

import java.util.List;

public class AutoSteal extends Module {
    public AutoSteal() {
        super(AutoplayAddon.autoplay, "auto-steal", "master thief");
    }
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<List<Item>> itemsToCollect = sgGeneral.add(new ItemListSetting.Builder()
        .name("items-to-collect")
        .description("Items to drop.")
        .build()
    );


    @EventHandler()
    private void onPreTick(TickEvent.Pre event) {
        if (itemsToCollect.get().isEmpty()) return;
        new Thread(() -> {
            if (ItemCollection.collect(itemsToCollect.get())) {
                ChatUtils.info("Item found");
            }
        }).start();
    }



}
