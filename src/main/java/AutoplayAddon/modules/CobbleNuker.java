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
import meteordevelopment.orbit.EventPriority;
import net.minecraft.block.BlockState;
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

public class CobbleNuker extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    public static final double MAX_INTERACTION_DISTANCE = MathHelper.square(6.0D) - 0.5;
    public Setting<Integer> blockrange = sgGeneral.add(new IntSetting.Builder()
        .name("Amount of logs to mine")
        .description("blockrange")
        .defaultValue(5)
        .min(0)
        .sliderMax(100)
        .build()
    );

    private Direction direction = Direction.UP;


    public CobbleNuker() {
        super(AutoplayAddon.autoplay, "CobbleNuker", "Attempts to instantly mine blocks.");
    }

    @EventHandler(priority = EventPriority.LOWEST)

    private void onTick(TickEvent.Pre event) {
        Packet.sendPacket(new PlayerMoveC2SPacket.OnGroundOnly(true));
        BlockState cobblestoneState = Blocks.COBBLESTONE.getDefaultState();
        BlockState stoneState = Blocks.STONE.getDefaultState();
        BlockPos blockPos = mc.player.getBlockPos();
        for (int x = -blockrange.get(); x <= blockrange.get(); x++) {
            for (int y = 0; y <= blockrange.get(); y++) {
                for (int z = -blockrange.get(); z <= blockrange.get(); z++) {
                    BlockPos currentBlockPos = new BlockPos(blockPos.getX() + x, blockPos.getY() + y, blockPos.getZ() + z);
                    if (!ServerSideValues.canSendPackets(2, System.nanoTime())) return;
                    if (mc.player.getEyePos().distanceTo(Vec3d.ofCenter(currentBlockPos)) > MAX_INTERACTION_DISTANCE) continue;
                    if (mc.world.getBlockState(currentBlockPos) == cobblestoneState ) {
                        Packet.sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.START_DESTROY_BLOCK, currentBlockPos, direction));
                        Packet.sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, currentBlockPos, direction));
                        mc.world.setBlockState(currentBlockPos, Blocks.AIR.getDefaultState());
                    }
                    if (mc.world.getBlockState(currentBlockPos) == stoneState ) {
                        Packet.sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.START_DESTROY_BLOCK, currentBlockPos, direction));
                        //Packet.sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, currentBlockPos, direction));
                        mc.world.setBlockState(currentBlockPos, Blocks.AIR.getDefaultState());
                    }

                }
            }
        }
    }


}
