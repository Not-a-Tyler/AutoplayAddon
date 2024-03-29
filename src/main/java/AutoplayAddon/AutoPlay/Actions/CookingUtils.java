package AutoplayAddon.AutoPlay.Actions;

import AutoplayAddon.AutoPlay.Controller.SmartGoto;
import AutoplayAddon.AutoPlay.Other.WaitUtil;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.meteorclient.utils.world.BlockUtils;
import net.minecraft.block.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.FurnaceScreenHandler;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import static meteordevelopment.meteorclient.MeteorClient.mc;
import java.util.List;

public class CookingUtils {

    public static void cook(Item itemToCook) {
        ChatUtils.info("going to furnace");
        Vec3d e = SmartGoto.gotoblock(List.of(Blocks.FURNACE));
        BlockPos epos = new BlockPos((int) Math.floor(e.getX()), (int) Math.floor(e.getY()), (int) Math.floor(e.getZ()));
        ChatUtils.info("Waiting before cooking");
        WaitUtil.wait1sec();

        Vec3d playerEyePos = mc.player.getEyePos();
        Vec3d vec3d = playerEyePos.add(e.subtract(playerEyePos).normalize().multiply(0.5));
        BlockHitResult blockHitResult = new BlockHitResult(vec3d, Direction.UP, epos, false);
        BlockUtils.interact(blockHitResult, Hand.MAIN_HAND, false);
        ChatUtils.info("clicked furnace, waiting before cooking");
        WaitUtil.wait1sec();
        int itemslot = InvUtils.find(itemToCook).slot();
        int fuelslot = InvUtils.find(Items.COAL).slot();
        ChatUtils.info("coal slot: " + fuelslot + " itemslot: " + itemslot);
        // Move the item to the furnace input slot (slot 0)
        InvUtils.move().from(itemslot).to(0);
        InvUtils.move().from(fuelslot).to(1);

        // Wait for the item to cook
        boolean itemCooked = false;
        while (!itemCooked) {
            if (mc.player.currentScreenHandler instanceof FurnaceScreenHandler furnaceScreenHandler) {
                ItemStack outputStack = furnaceScreenHandler.getSlot(2).getStack();
                if (!outputStack.isEmpty() && outputStack.getItem() != itemToCook) {
                    itemCooked = true;
                } else {
                    WaitUtil.wait1sec();
                }
            }
        }
        ChatUtils.info("Item cooked, retrieving from furnace");
        InvUtils.move().from(2).toHotbar(0);
    }
}
