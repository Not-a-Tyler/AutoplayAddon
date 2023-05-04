package AutoplayAddon.commands;
import AutoplayAddon.utils.*;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import meteordevelopment.meteorclient.commands.Command;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import net.minecraft.command.CommandSource;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.recipe.Recipe;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.screen.slot.SlotActionType;
import static meteordevelopment.meteorclient.MeteorClient.mc;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;

public class testcommand9 extends Command {

    public testcommand9() {
        super("craftastick", "crafts 2 sticks");
    }

    @Override
    public void build(LiteralArgumentBuilder<CommandSource> builder) {
        builder.executes(context -> {
            Recipe<?> recipeToCraft = findRecipeForItem(Items.STICK, 4);

            mc.interactionManager.clickRecipe(mc.player.currentScreenHandler.syncId, recipeToCraft, false);
            mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, 0, 0, SlotActionType.QUICK_MOVE, mc.player);
            return SINGLE_SUCCESS;
        });
    }

    private static Recipe<?> findRecipeForItem(Item itemToCraft, int desiredOutputCount) {
        DynamicRegistryManager registryManager = mc.world.getRegistryManager();
        for (Recipe<?> recipe : mc.world.getRecipeManager().values()) {
            ItemStack outputStack = recipe.getOutput(registryManager);
            if (outputStack.getItem() == itemToCraft && outputStack.getCount() == desiredOutputCount) {
                return recipe;
            }
        }
        ChatUtils.info("recipe not found");
        return null;
    }


}
