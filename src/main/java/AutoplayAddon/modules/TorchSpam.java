package AutoplayAddon.modules;

import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.meteorclient.utils.player.SlotUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import AutoplayAddon.AutoplayAddon;
import AutoplayAddon.AutoPlay.Other.Packet;
import AutoplayAddon.Tracker.ServerSideValues;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.systems.modules.Module;
import net.minecraft.util.math.Direction;




public class TorchSpam extends Module {
    public TorchSpam() {
        super(AutoplayAddon.autoplay, "torch-spam", "bypass live overflows movement checks");
    }
    private final SettingGroup sgGeneral = settings.getDefaultGroup();


    private int findTorch() {
        for (int i = 0; i < mc.player.getInventory().size(); i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (stack.getItem() == net.minecraft.item.Items.TORCH) {
                return i;
            }
        }
        return -1; // If not found
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        BlockPos playerPos = mc.player.getBlockPos();

        ItemStack offhandStack = mc.player.getOffHandStack();
        if (offhandStack.isEmpty()) {
            int torchSlot = findTorch();
            if (torchSlot != -1) {
                InvUtils.move().from(torchSlot).to(SlotUtils.OFFHAND);
            }
        }

        // Iterate over a 3 block radius
        for (int x = -3; x <= 3; x++) {
            for (int y = -3; y <= 3; y++) {
                for (int z = -3; z <= 3; z++) {
                    BlockPos checkPos = playerPos.add(x, y, z);
                    if (!ServerSideValues.canPlace()) return;
                    if (mc.world.getBlockState(checkPos).getBlock() == Blocks.GRASS_BLOCK ||
                        mc.world.getBlockState(checkPos).getBlock() == Blocks.DIRT) {

                        BlockPos placePos = checkPos.up();
                        if (mc.world.getBlockState(placePos).isAir()) {
                            Packet.sendPacket(new PlayerInteractBlockC2SPacket(Hand.OFF_HAND, new BlockHitResult(placePos.toCenterPos(), Direction.UP, placePos, false), 0));
                            ServerSideValues.handleUse();
                        }
                    }
                }
            }
        }
    }
}
