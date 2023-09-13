package AutoplayAddon.modules;

import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.item.Item;
import net.minecraft.item.Items;  // Import the item you are looking for
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket;
import net.minecraft.util.Hand;
import AutoplayAddon.AutoplayAddon;

public class AutoHorn extends Module {
    private final Item targetItem = Items.GOAT_HORN;  // Replace with your target item
    private int previousSlot;

    public AutoHorn() {
        super(AutoplayAddon.autoplay, "auto-horn", "Automatically uses a specific item from the hotbar when available.");
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        int targetSlot = findTargetItemSlot();

        // If the item isn't found or is on cooldown, do nothing
        if (mc.player.getItemCooldownManager().isCoolingDown(targetItem)) return;

        previousSlot = mc.player.getInventory().selectedSlot;
        mc.getNetworkHandler().sendPacket(new UpdateSelectedSlotC2SPacket(targetSlot));
        mc.getNetworkHandler().sendPacket(new PlayerInteractItemC2SPacket(Hand.MAIN_HAND, mc.player.currentScreenHandler.syncId));
        mc.getNetworkHandler().sendPacket(new UpdateSelectedSlotC2SPacket(previousSlot));
    }

    private int findTargetItemSlot() {
        for (int i = 0; i < 9; i++) {
            if (mc.player.getInventory().getStack(i).getItem() == targetItem) {
                return i;
            }
        }
        return -1;
    }

}
