package AutoplayAddon.modules;

import AutoplayAddon.AutoPlay.Mining.MineUtils;
import AutoplayAddon.AutoPlay.Other.Packet;
import AutoplayAddon.AutoplayAddon;
import AutoplayAddon.Tracker.ServerSideValues;
import meteordevelopment.meteorclient.events.entity.player.StartBreakingBlockEvent;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.BlockState;
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.util.math.BlockPos;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Box;
import net.minecraft.util.shape.VoxelShape;


public class BetterMine extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgRender = settings.createGroup("Render");
    private final Setting<Boolean> swinghandclient = sgGeneral.add(new BoolSetting.Builder()
        .name("swing-hand-client")
        .description("Clients will copy the servers sneaking status")
        .defaultValue(true)
        .build()
    );
    private final Setting<Boolean> swinghandserver = sgGeneral.add(new BoolSetting.Builder()
        .name("swing-hand-server")
        .description("Clients will copy the servers sneaking status")
        .defaultValue(false)
        .build()
    );

    private final Setting<Boolean> mineSecondary = sgGeneral.add(new BoolSetting.Builder()
        .name("secondary-block")
        .description("Clients will copy the servers sneaking status")
        .defaultValue(false)
        .build()
    );

    private final Setting<ShapeMode> shapeMode = sgRender.add(new EnumSetting.Builder<ShapeMode>()
        .name("shape-mode")
        .description("How the shapes are rendered.")
        .defaultValue(ShapeMode.Both)
        .build()
    );

    private final Setting<SettingColor> startColor = sgRender.add(new ColorSetting.Builder()
        .name("start-color")
        .description("The color for the non-broken block.")
        .defaultValue(new SettingColor(25, 252, 25, 150))
        .build()
    );


    private final Setting<SettingColor> endColor = sgRender.add(new ColorSetting.Builder()
        .name("end-color")
        .description("The color for the fully-broken block.")
        .defaultValue(new SettingColor(255, 25, 25, 150))
        .build()
    );

    private final Color cSides = new Color();
    private final Color cLines = new Color();
    private int startTick;
    private float destroyProgressFast, destroyProgressSlow;
    private Direction direction = Direction.UP;

    public BetterMine() {
        super(AutoplayAddon.autoplay, "better-mine", "Attempts to instantly mine blocks.");
    }


    private BlockState fastBlockState = null, secondaryBlockState = null;
    private BlockPos fastBlockPos = null, secondaryBlockPos = null;

    @EventHandler
    private void onStartBreakingBlock(StartBreakingBlockEvent event) {
        event.cancel();
        event.setCancelled(true);
        if (fastBlockPos != null || secondaryBlockPos != null) return;
        destroyProgressFast = 0;
        destroyProgressSlow = 0;
        startTick = ServerSideValues.ticks;
        BlockPos hitBlockPos = event.blockPos;

        BlockPos downBlock = hitBlockPos.down();
        BlockState downBlockState = mc.world.getBlockState(downBlock);

        secondaryBlockState = mc.world.getBlockState(hitBlockPos);
        secondaryBlockPos = hitBlockPos;


        if (!downBlockState.isSolid() || !mineSecondary.get()) {
            secondaryBlockPos = null;
            fastBreak(hitBlockPos);
            return;
        }

        if (MineUtils.canInstaBreak(secondaryBlockState, hitBlockPos)) {
            ChatUtils.info("We can Insta Break the bottom below");
            fastBreak(downBlock);
            fastBreak(hitBlockPos);
            return;
        }
        if (MineUtils.canInstaBreak(downBlockState, downBlock)) {
            ChatUtils.info("We can Insta Break the block");
            fastBreak(hitBlockPos);
            fastBreak(downBlock);
            return;
        }


        Packet.sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.START_DESTROY_BLOCK, hitBlockPos, direction));
        Packet.sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, hitBlockPos, direction));

        fastBreak(hitBlockPos.down());
    }


    private void fastBreak(BlockPos blockPos) {

        fastBlockState = mc.world.getBlockState(blockPos);
        fastBlockPos = blockPos;

        if (swinghandclient.get()) mc.player.swingHand(mc.player.getActiveHand());
        if (swinghandserver.get()) Packet.sendPacket(new HandSwingC2SPacket(mc.player.getActiveHand()));
        Packet.sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.START_DESTROY_BLOCK, blockPos, direction));
        float f1 = fastBlockState.calcBlockBreakingDelta(mc.player, mc.world, blockPos);
        float f2 = MineUtils.calcBlockBreakingDelta((BlockState) fastBlockState, blockPos);

        if (f1 != f2) ChatUtils.info("Block breaking delta is different: " + f1 + " " + f2);

        if (f1 >= 1.0F) {
            ChatUtils.info("Insta Break with progress: " + f1);
            mc.world.setBlockState(blockPos, Blocks.AIR.getDefaultState());
            fastBlockPos = null;
            fastBlockState = null;
            return;
        }

        if (f1 >= 0.7F) {
            Packet.sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, blockPos, direction));
            ChatUtils.info("Block broken at start with progress: " + f1);
            mc.world.setBlockState(blockPos, Blocks.AIR.getDefaultState());
            fastBlockPos = null;
            fastBlockState = null;
        }
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        int l = ServerSideValues.ticks - startTick;
        if (fastBlockPos != null || secondaryBlockPos != null) {
            if (swinghandclient.get()) mc.player.swingHand(mc.player.getActiveHand());
            if (swinghandserver.get()) Packet.sendPacket(new HandSwingC2SPacket(mc.player.getActiveHand()));
        }
        if (fastBlockPos != null) {
            float fast = fastBlockState.calcBlockBreakingDelta(mc.player, mc.world, fastBlockPos) * (float) (l + 1);
            if (MineUtils.calcBlockBreakingDelta((BlockState) fastBlockState, fastBlockPos) != fastBlockState.calcBlockBreakingDelta(mc.player, mc.world, fastBlockPos)) ChatUtils.info("Block breaking delta is different fast: " + fast);
            if (MineUtils.calcBlockBreakingDelta((BlockState) fastBlockState, fastBlockPos) == fastBlockState.calcBlockBreakingDelta(mc.player, mc.world, fastBlockPos)) ChatUtils.info("its the same" + MineUtils.calcBlockBreakingDelta((BlockState) fastBlockState, fastBlockPos) + " " + fastBlockState.calcBlockBreakingDelta(mc.player, mc.world, fastBlockPos));

            destroyProgressFast = (fast / 0.7F); // Convert the range here.
            if (fast >= 0.7F) {
                Packet.sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, fastBlockPos, direction));
                mc.world.setBlockState(fastBlockPos, Blocks.AIR.getDefaultState());
                fastBlockPos = null;
                fastBlockState = null;
                destroyProgressFast = 0;
            }
        }
        if (secondaryBlockPos != null) {
            float slow = secondaryBlockState.calcBlockBreakingDelta(mc.player, mc.world, secondaryBlockPos) * (float) (l + 1);
            destroyProgressSlow = slow; // Convert the range here.
            if (slow >= 1.0F) {
                ChatUtils.info("Removed slow block: " + slow);
                mc.world.setBlockState(secondaryBlockPos, Blocks.AIR.getDefaultState());
                secondaryBlockPos = null;
                secondaryBlockState = null;
                destroyProgressSlow = 0;
            }
        }
    }


    @EventHandler
    private void onRender(Render3DEvent event) {
        if (fastBlockPos != null) {
            renderBlockFromPos(event, fastBlockPos, destroyProgressFast);
        }
        if (secondaryBlockPos != null) {
            renderBlockFromPos(event, secondaryBlockPos, destroyProgressSlow);
        }
    }
    private void renderBlockFromPos(Render3DEvent event, BlockPos blockPos, double progress) {
        BlockState state = mc.world.getBlockState(blockPos);
        VoxelShape shape = state.getOutlineShape(mc.world, blockPos);
        if (shape == null || shape.isEmpty()) return;

        Box orig = shape.getBoundingBox();

        double shrinkFactor = 1d - progress;

        renderBlock(event, orig, blockPos, shrinkFactor, progress);
    }

    private void renderBlock(Render3DEvent event, Box orig, BlockPos pos, double shrinkFactor, double progress) {
        Box box = orig.shrink(
            orig.getLengthX() * shrinkFactor,
            orig.getLengthY() * shrinkFactor,
            orig.getLengthZ() * shrinkFactor
        );

        double xShrink = (orig.getLengthX() * shrinkFactor) / 2;
        double yShrink = (orig.getLengthY() * shrinkFactor) / 2;
        double zShrink = (orig.getLengthZ() * shrinkFactor) / 2;

        double x1 = pos.getX() + box.minX + xShrink;
        double y1 = pos.getY() + box.minY + yShrink;
        double z1 = pos.getZ() + box.minZ + zShrink;
        double x2 = pos.getX() + box.maxX + xShrink;
        double y2 = pos.getY() + box.maxY + yShrink;
        double z2 = pos.getZ() + box.maxZ + zShrink;

        Color c1Sides = startColor.get().copy().a(startColor.get().a / 2);
        Color c2Sides = endColor.get().copy().a(endColor.get().a / 2);

        cSides.set(
            (int) Math.round(c1Sides.r + (c2Sides.r - c1Sides.r) * progress),
            (int) Math.round(c1Sides.g + (c2Sides.g - c1Sides.g) * progress),
            (int) Math.round(c1Sides.b + (c2Sides.b - c1Sides.b) * progress),
            (int) Math.round(c1Sides.a + (c2Sides.a - c1Sides.a) * progress)
        );

        Color c1Lines = startColor.get();
        Color c2Lines = endColor.get();

        cLines.set(
            (int) Math.round(c1Lines.r + (c2Lines.r - c1Lines.r) * progress),
            (int) Math.round(c1Lines.g + (c2Lines.g - c1Lines.g) * progress),
            (int) Math.round(c1Lines.b + (c2Lines.b - c1Lines.b) * progress),
            (int) Math.round(c1Lines.a + (c2Lines.a - c1Lines.a) * progress)
        );

        event.renderer.box(x1, y1, z1, x2, y2, z2, cSides, cLines, shapeMode.get(), 0);
    }
}


