package AutoplayAddon.Tracker;

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

    public void addChunk(Chunk chunk) {
        executorService.submit(() -> {
            for (int x = 0; x < 16; x++) {
                for (int y = 0; y < 256; y++) {
                    for (int z = 0; z < 16; z++) {
                        BlockPos pos = new BlockPos(chunk.getPos().getStartX() + x, y, chunk.getPos().getStartZ() + z);
                        BlockState blockState = chunk.getBlockState(pos);
                        Block block = chunk.getBlockState(pos).getBlock();
                        if (!blockState.isSolid()) {
                            continue;
                        }
                        synchronized (blockMap) {
                            blockMap.computeIfAbsent(block, k -> new ArrayList<>()).add(new BlockData(pos, block));
                        }
                    }
                }
            }
        });
    }

    public void removeChunk(Chunk chunk) {
        executorService.submit(() -> {
            synchronized (blockMap) {
                for (Block block : blockMap.keySet()) {
                    blockMap.get(block).removeIf(data -> data.getPos().getX() >= chunk.getPos().getStartX() && data.getPos().getX() < chunk.getPos().getStartX() + 16 && data.getPos().getZ() >= chunk.getPos().getStartZ() && data.getPos().getZ() < chunk.getPos().getStartZ() + 16);
                }
            }
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
