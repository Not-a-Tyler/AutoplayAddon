package AutoplayAddon.Tracker;
import AutoplayAddon.AutoplayAddon;
import meteordevelopment.meteorclient.events.game.GameLeftEvent;
import meteordevelopment.meteorclient.events.world.BlockUpdateEvent;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.orbit.EventPriority;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.Chunk;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static meteordevelopment.meteorclient.MeteorClient.mc;

public class BlockCache {

    public BlockPos lastBlockPos = null; // Added field to store the last block position
    public final Map<Block, List<BlockData>> blockMap = new HashMap<>();
    public ExecutorService executorService = Executors.newSingleThreadExecutor();

    public BlockCache() {
    }

    @EventHandler(priority = EventPriority.HIGHEST + 1)
    private void onSendMovePacket(GameLeftEvent event) {
        AutoplayAddon.LOG.info("removing all");
        blockMap.clear();
    }

    @EventHandler(priority = EventPriority.HIGHEST + 1)
    private void onBlockPosUpdate(BlockUpdateEvent event) {
        BlockPos updatedPos = event.pos;
        BlockState newState = event.newState;
        BlockState oldState = event.oldState;

        Block updatedBlock = newState.getBlock();

        // If the block's new state is solid, add it to the blockMap
        if (newState.isSolid()) {
            synchronized (blockMap) {
                blockMap.computeIfAbsent(updatedBlock, k -> new ArrayList<>()).add(new BlockData(updatedPos, updatedBlock));
            }
        }

        // If the block's old state was solid and the new state is not, remove it from the blockMap
        if (oldState.isSolid() && !newState.isSolid()) {
            synchronized (blockMap) {
                List<BlockData> blockDataList = blockMap.get(updatedBlock);
                if (blockDataList != null) {
                    blockDataList.removeIf(blockData -> blockData.getPos().equals(updatedPos));
                }
            }
        }
    }


    public void addChunk(Chunk chunk) {
        executorService.submit(() -> {
            for (int x = 0; x < 16; x++) {
                for (int y = 0; y < 256; y++) {
                    for (int z = 0; z < 16; z++) {
                        BlockPos pos = new BlockPos(chunk.getPos().getStartX() + x, y, chunk.getPos().getStartZ() + z);
                        BlockState blockState = chunk.getBlockState(pos);
                        Block block = blockState.getBlock();
                        if (blockState.isSolid()) {
                            synchronized (blockMap) {
                                blockMap.computeIfAbsent(block, k -> new ArrayList<>()).add(new BlockData(pos, block));
                            }
                        }
                    }
                }
            }
        });
    }


    public void removeChunk(Chunk chunk) {
        executorService.submit(() -> {
            blockMap.forEach((block, blockDataList) -> blockDataList.removeIf(data ->
                data.getPos().getX() >= chunk.getPos().getStartX() &&
                    data.getPos().getX() < chunk.getPos().getStartX() + 16 &&
                    data.getPos().getZ() >= chunk.getPos().getStartZ() &&
                    data.getPos().getZ() < chunk.getPos().getStartZ() + 16
            ));
        });
    }


    public BlockPos getNearestBlock(Block targetBlock) {
        synchronized (blockMap) {
            List<BlockData> blockDataList = blockMap.get(targetBlock);
            if (blockDataList == null || blockDataList.isEmpty()) return null;
            if (mc.player == null) return null;
            BlockPos playerPos = mc.player.getBlockPos();
            lastBlockPos = blockDataList.stream().min(Comparator.comparingDouble(a -> a.getPos().getSquaredDistance(playerPos))).get().getPos();
            return lastBlockPos;
        }
    }

    public boolean blockExistsAt(BlockPos targetPos) {
        return blockMap.values().stream().anyMatch(blockDataList -> blockDataList.stream().anyMatch(blockData -> blockData.getPos().equals(targetPos)));
    }


    public static class BlockData {
        private final BlockPos pos;
        private final Block block;

        public BlockData(BlockPos pos, Block block) {
            this.pos = pos;
            this.block = block;
        }

        public BlockPos getPos() {
            return pos;
        }
    }

}
