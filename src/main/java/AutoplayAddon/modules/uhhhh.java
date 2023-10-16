package AutoplayAddon.modules;


import AutoplayAddon.AutoPlay.Locator.CanPickUpTest;
import AutoplayAddon.AutoPlay.Movement.GotoUtil;
import AutoplayAddon.AutoplayAddon;
import AutoplayAddon.AutoPlay.Other.Packet;
import AutoplayAddon.Tracker.BlockCache;
import AutoplayAddon.Tracker.ServerSideValues;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

import java.util.Collections;
import java.util.List;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;
import static meteordevelopment.meteorclient.MeteorClient.mc;

public class uhhhh extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    public static final double MAX_INTERACTION_DISTANCE = MathHelper.square(6.0D);
    public Setting<Integer> amount = sgGeneral.add(new IntSetting.Builder()
        .name("Amount of logs to mine")
        .description("test")
        .defaultValue(20)
        .min(0)
        .sliderMax(100)
        .build()
    );
    public Setting<Integer> blockrange = sgGeneral.add(new IntSetting.Builder()
        .name("Amount of logs to mine")
        .description("blockrange")
        .defaultValue(5)
        .min(0)
        .sliderMax(100)
        .build()
    );

    private Direction direction = Direction.UP;


    public uhhhh() {
        super(AutoplayAddon.autoplay, "uhhhh", "Attempts to instantly mine blocks.");
    }

    @Override
    public void onActivate() {
        new Thread(() -> {
            while (true) {
                List<Item> targetBlocks6 = Collections.singletonList(Items.COBBLESTONE);
                List<Vec3d> collectableBlock = CanPickUpTest.findCollectableItem(targetBlocks6);
                if (collectableBlock == null) {
                    ChatUtils.info("No blocks found");
                    toggle();
                    break;
                }
                Vec3d airGapPos = collectableBlock.get(1);
                BlockPos blockPos = new BlockPos((int) airGapPos.x, (int) airGapPos.y, (int) airGapPos.z);
                GotoUtil.setPos(airGapPos, false, true, true);
                for (int x = -blockrange.get(); x <= blockrange.get(); x++) {
                    for (int y = -blockrange.get(); y <= blockrange.get(); y++) {
                        for (int z = -blockrange.get(); z <= blockrange.get(); z++) {
                            // Calculate the position of the block we're checking
                            BlockPos currentBlockPos = new BlockPos(blockPos.getX() + x, blockPos.getY() + y, blockPos.getZ() + z);
                            if (!ServerSideValues.canSendPackets(2, System.nanoTime())) continue;
                            if (mc.world.getBlockState(currentBlockPos).isSolid()) {
                                ChatUtils.info("sending packet to break block at " + currentBlockPos);
                                Packet.sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.START_DESTROY_BLOCK, currentBlockPos, direction));
                                Packet.sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, currentBlockPos, direction));
                            }
                        }
                    }
                }
            }
        }).start();
    }


}
