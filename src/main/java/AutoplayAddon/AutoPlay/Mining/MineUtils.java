package AutoplayAddon.AutoPlay.Mining;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.BlockState;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.effect.StatusEffectUtil;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.util.math.BlockPos;
import static meteordevelopment.meteorclient.MeteorClient.mc;

public class MineUtils {

    public static float calcBlockBreakingDelta(BlockState state, BlockPos pos) {
        float g = state.getHardness(mc.world, pos);
        if (g == -1.0f) {
            return 0.0f;
        }
        int h = mc.player.canHarvest(state) ? 30 : 100;


        float f = mc.player.getMainHandStack().getMiningSpeedMultiplier(state);

        if (f > 1.0f) {
            int i = EnchantmentHelper.getEfficiency(mc.player);
            ItemStack itemStack = mc.player.getMainHandStack();
            if (i > 0 && !itemStack.isEmpty()) {
                f += (float)(i * i + 1);
            }
        }
        if (StatusEffectUtil.hasHaste(mc.player)) {
            f *= 1.0f + (float)(StatusEffectUtil.getHasteAmplifier(mc.player) + 1) * 0.2f;
        }
        if (mc.player.hasStatusEffect(StatusEffects.MINING_FATIGUE)) {
            f *= (switch (mc.player.getStatusEffect(StatusEffects.MINING_FATIGUE).getAmplifier()) {
                case 0 -> 0.3f;
                case 1 -> 0.09f;
                case 2 -> 0.0027f;
                default -> 8.1E-4f;
            });
        }
        if (mc.player.isSubmergedIn(FluidTags.WATER) && !EnchantmentHelper.hasAquaAffinity(mc.player)) {
            f /= 5.0f;
        }

//        if (!this.isOnGround()) {
//            f /= 5.0f;
//        }

        return f / g / (float)h;
    }

    public static boolean canInstaBreak(BlockState blockState, BlockPos blockPos) {
        if (calcBlockBreakingDelta(blockState, blockPos) >= 0.7F) return true;
        return false;
    }

}
