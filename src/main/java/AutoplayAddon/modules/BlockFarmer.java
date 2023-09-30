package AutoplayAddon.modules;

import AutoplayAddon.AutoplayAddon;
import AutoplayAddon.Tracker.ServerSideValues;
import meteordevelopment.meteorclient.events.entity.player.StartBreakingBlockEvent;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.meteorclient.utils.player.SlotUtils;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

public class BlockFarmer extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final Setting<Boolean> what = sgGeneral.add(new BoolSetting.Builder()
        .name("break-block")
        .description("Renders a block overlay where you will be teleported.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> echest = sgGeneral.add(new BoolSetting.Builder()
        .name("auto-echest")
        .description("Renders a block overlay where you will be teleported.")
        .defaultValue(true)
        .build()
    );
    private final SettingGroup sgRender = settings.createGroup("Render");

    private final Setting<ShapeMode> shapeMode = sgRender.add(new EnumSetting.Builder<ShapeMode>()
        .name("shape-mode")
        .description("How the shapes are rendered.")
        .defaultValue(ShapeMode.Both)
        .build()
    );

    private final Setting<SettingColor> sideColor = sgRender.add(new ColorSetting.Builder()
        .name("side-color")
        .description("The color of the sides of the blocks being rendered.")
        .defaultValue(new SettingColor(204, 0, 0, 10))
        .build()
    );

    private final Setting<SettingColor> lineColor = sgRender.add(new ColorSetting.Builder()
        .name("line-color")
        .description("The color of the lines of the blocks being rendered.")
        .defaultValue(new SettingColor(204, 0, 0, 255))
        .build()
    );

    private BlockPos blockPos = new BlockPos(0, -1, 0);
    private Direction direction = Direction.UP;

    public BlockFarmer() {
        super(AutoplayAddon.autoplay, "block-farmer", "Attempts to instantly mine blocks.");
    }

    @Override
    public void onActivate() {
        blockPos = null;
    }

    @EventHandler
    private void onStartBreakingBlock(StartBreakingBlockEvent event) {
        ChatUtils.info("Start breaking block event");
        direction = event.direction;
        blockPos = event.blockPos;
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (blockPos == null) return;
        ItemStack offhandStack = mc.player.getOffHandStack();
        if (echest.get() && offhandStack.isEmpty()) {
            int enderChestSlot = findEnderChest();
            if (enderChestSlot != -1) {
                InvUtils.move().from(enderChestSlot).to(SlotUtils.OFFHAND);
            }
        }

        while (ServerSideValues.canPlace()) {
            if (what.get()) {
                mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, blockPos, direction));
            }
            mc.getNetworkHandler().sendPacket(new PlayerInteractBlockC2SPacket(Hand.OFF_HAND, new BlockHitResult(blockPos.toCenterPos(), direction, blockPos, false), 0));
        }
    }

    // New method to replenish ender chest in off-hand
    private int findEnderChest() {
        for (int i = 0; i < mc.player.getInventory().size(); i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (stack.getItem() == net.minecraft.item.Items.ENDER_CHEST) {
                return i;
            }
        }
        return -1; // If not found
    }


    @EventHandler
    private void onRender(Render3DEvent event) {
        if (blockPos == null) return;
        event.renderer.box(blockPos, sideColor.get(), lineColor.get(), shapeMode.get(), 0);
    }
}
