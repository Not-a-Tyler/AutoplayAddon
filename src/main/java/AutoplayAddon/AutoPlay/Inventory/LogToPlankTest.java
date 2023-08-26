package AutoplayAddon.AutoPlay.Inventory;

import AutoplayAddon.AutoPlay.Actions.CraftUtil;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.meteorclient.utils.player.FindItemResult;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import net.minecraft.item.Item;

import java.util.ArrayList;
import java.util.List;


public class LogToPlankTest {
    public record ItemSlotPair(Item item, FindItemResult slot) {
    }
    public static void Log2Plank() {
        List<ItemSlotPair> itemSlotPairs = new ArrayList<>();

        for (Item currentItem : Lists.LOG) {
            FindItemResult slot = InvUtils.find(currentItem);
            if ((slot.slot()) != -1) {
                itemSlotPairs.add(new ItemSlotPair(currentItem, slot));
            }
        }

        for (ItemSlotPair itemSlotPair : itemSlotPairs) {
            Item currentItem = itemSlotPair.item();
            Item plank = Lists.logToPlanks(currentItem);
            FindItemResult slot = itemSlotPair.slot();
            CraftUtil.craftItem(plank, slot.count());
            ChatUtils.info("Item: "  + plank.getName().getString() + ", Slot: " + slot.slot());
        }
    }

}
