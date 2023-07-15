package AutoplayAddon.AutoPlay.Inventory;

import AutoplayAddon.AutoPlay.Actions.CraftUtil;
import AutoplayAddon.AutoPlay.Inventory.Lists;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.meteorclient.utils.player.FindItemResult;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import net.minecraft.item.Item;

import java.util.ArrayList;
import java.util.List;

import AutoplayAddon.AutoPlay.Inventory.Lists;


public class LogToPlankTest {
    public static class ItemSlotPair {
        private Item item;
        private FindItemResult slot;

        public ItemSlotPair(Item item, FindItemResult slot) {
            this.item = item;
            this.slot = slot;
        }

        public Item getItem() {
            return item;
        }

        public FindItemResult getSlot() {
            return slot;
        }
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
            Item currentItem = itemSlotPair.getItem();
            Item plank = Lists.logToPlanks(currentItem);
            FindItemResult slot = itemSlotPair.getSlot();
            CraftUtil.craftItem(plank, slot.count());
            ChatUtils.info("Item: "  + plank.getName().getString() + ", Slot: " + slot.slot());
        }
    }

}
