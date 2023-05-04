package AutoplayAddon.utils;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.ShapedRecipe;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.registry.DynamicRegistryManager;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.meteorclient.utils.world.BlockUtils;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import java.util.List;


public class CraftUtil {
    private static final MinecraftClient mc = MinecraftClient.getInstance();

    public static ItemStack craftItem(Item itemToCraft, int craftCount) {
        Recipe<?> recipeToCraft = findRecipeForItem(itemToCraft);
        boolean needsCraftingTable = needsCraftingTable(recipeToCraft);
        if (recipeToCraft != null) {
            for (int i = 0; i < craftCount; i++) {
                if (needsCraftingTable) {
                    ChatUtils.info("going to crafting table");
                    Vec3d e = SmartGoto.gotoblock(List.of(Blocks.CRAFTING_TABLE));
                    BlockPos epos = new BlockPos((int) Math.floor(e.getX()), (int) Math.floor(e.getY()), (int) Math.floor(e.getZ()));
                    ChatUtils.info("going to crafting table at: " + e + " Blockpos: " + epos.toShortString());
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException ee) {
                        ee.printStackTrace();
                    }
                    Vec3d playerEyePos = mc.player.getEyePos();
                    Vec3d vec3d = playerEyePos.add(e.subtract(playerEyePos).normalize().multiply(0.5));
                    BlockHitResult blockHitResult = new BlockHitResult(vec3d, Direction.UP, epos, false);
                    ChatUtils.info("waiting before clicking table");

                    BlockUtils.interact(blockHitResult, Hand.MAIN_HAND, false);
                    ChatUtils.info("clicked table, waiting before crafting");
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException ee) {
                        ee.printStackTrace();
                    }
                    mc.interactionManager.clickRecipe(mc.player.currentScreenHandler.syncId, recipeToCraft, false);
                    mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, 0, 0, SlotActionType.QUICK_MOVE, mc.player);
                    ChatUtils.info("Crafted " + itemToCraft +" with Crafting Table");
                } else {
                    mc.interactionManager.clickRecipe(mc.player.currentScreenHandler.syncId, recipeToCraft, false);
                    mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, 0, 0, SlotActionType.QUICK_MOVE, mc.player);
                    ChatUtils.info("Crafted " + itemToCraft);
                }
            }
            int craftedSlot = InvUtils.find(itemToCraft).slot();
            if (craftedSlot != -1) {
                InvUtils.move().from(craftedSlot).toHotbar(0);
                return new ItemStack(itemToCraft);
            }
        } else {
            ChatUtils.info("no recipe provided");
        }
        return null;
    }

    private static Recipe<?> findRecipeForItem(Item itemToCraft) {
        DynamicRegistryManager registryManager = mc.world.getRegistryManager();
        Recipe<?> bestRecipe = null;
        int maxOutputCount = 0;

        for (Recipe<?> recipe : mc.world.getRecipeManager().values()) {
            ItemStack outputStack = recipe.getOutput(registryManager);
            if (outputStack.getItem() == itemToCraft) {
                if (outputStack.getCount() > maxOutputCount) {
                    maxOutputCount = outputStack.getCount();
                    bestRecipe = recipe;
                }
            }
        }

        if (bestRecipe != null) {
            return bestRecipe;
        } else {
            ChatUtils.info("recipe not found");
            return null;
        }
    }


    private static boolean needsCraftingTable(Recipe<?> recipe) {
        if (recipe instanceof ShapedRecipe) {
            ShapedRecipe shapedRecipe = (ShapedRecipe) recipe;
            int width = shapedRecipe.getWidth();
            int height = shapedRecipe.getHeight();
            return width > 2 || height > 2;
        }
        return false;
    }
}
