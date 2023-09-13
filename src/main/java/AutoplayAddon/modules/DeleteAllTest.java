package AutoplayAddon.modules;

import AutoplayAddon.AutoPlay.Movement.GotoUtil;
import AutoplayAddon.AutoPlay.Movement.Movement;
import AutoplayAddon.AutoplayAddon;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import net.minecraft.block.ChestBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.block.entity.ShulkerBoxBlockEntity;
import net.minecraft.block.enums.ChestType;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.chunk.WorldChunk;
import net.minecraft.util.math.Direction;

public class DeleteAllTest extends Module {

    public DeleteAllTest() {
        super(AutoplayAddon.autoplay, "delete-all-containers", "master thief");
    }

    @Override
    public void onActivate() {
        ChatUtils.info("activating");
        int range = 5;  // 5 chunks around the player
        BlockPos playerPos = mc.player.getBlockPos();
        ChunkPos playerChunk = new ChunkPos(playerPos);

        // Iterate over the chunks around the player
        for (int dx = -range; dx <= range; dx++) {
            for (int dz = -range; dz <= range; dz++) {
                WorldChunk chunk = mc.world.getChunk(playerChunk.x + dx, playerChunk.z + dz);

                // Iterate over all block entities in the chunk
                for (BlockEntity entity : chunk.getBlockEntities().values()) {
                    // Check if block entity is an instance of a chest
                    if (entity instanceof ChestBlockEntity) {
                        BlockPos pos = entity.getPos();
                        ChatUtils.info("starting");
                        Thread waitForTickEventThread1 = new Thread(() -> {
                            Vec3d startingPos = mc.player.getPos();
                            Boolean ignore = false;
                            if (!Movement.AIDSboolean) {
                                GotoUtil.init(false);
                            } else {
                                ignore = true;
                            }
                            double x = pos.getX() + 0.5;
                            double y = pos.getY() + 2;
                            double z = pos.getZ() + 0.5;
                            GotoUtil.setPos(new Vec3d(x, y, z));
                            mc.getNetworkHandler().sendPacket(new PlayerInteractBlockC2SPacket(Hand.OFF_HAND, new BlockHitResult(pos.toCenterPos(), Direction.UP, pos, false), 0));
                            BlockEntity blockEntity = mc.world.getBlockEntity(pos);
                            int slots = 0;
                            if (blockEntity instanceof ChestBlockEntity) {
                                ChestType chestType = ((ChestBlockEntity) blockEntity).getCachedState().get(ChestBlock.CHEST_TYPE);
                                if (chestType == ChestType.SINGLE) {
                                    slots = 27;
                                } else if (chestType == ChestType.LEFT || chestType == ChestType.RIGHT) {
                                    slots = 54;
                                }
                            } else if (blockEntity instanceof ShulkerBoxBlockEntity) {
                                slots = 27;
                            }
                            ChatUtils.info("starting with slots " + slots);
                            for (int i = 0; i < slots; i++) {
                                //mc.getNetworkHandler().sendPacket(new ClickSlotC2SPacket(ServerSideValues.lastSyncId + 1, 0, i, 120, SlotActionType.SWAP, ItemStack.EMPTY, 0));
                               // mc.interactionManager.clickSlot(ServerSideValues.lastSyncId + 1, i, 120, SlotActionType.SWAP, mc.player);
                            }

                            GotoUtil.setPos(startingPos);
                            if (!ignore) {
                                ChatUtils.info("ending");
                                GotoUtil.disable();
                            }
                        });
                        waitForTickEventThread1.start();
                        toggle();
                    }
                }
            }
        }
    }

}
