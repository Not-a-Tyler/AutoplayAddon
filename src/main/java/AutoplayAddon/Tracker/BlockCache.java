package AutoplayAddon.Tracker;

import AutoplayAddon.AutoplayAddon;
import meteordevelopment.meteorclient.events.game.GameLeftEvent;
import meteordevelopment.meteorclient.events.world.BlockUpdateEvent;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.orbit.EventPriority;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.Chunk;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static meteordevelopment.meteorclient.MeteorClient.mc;

public class BlockCache {

    public BlockPos lastBlockPos = null;
    public final Map<BlockPos, Block> blockMap = new ConcurrentHashMap<>();
    public ExecutorService executorService = Executors.newSingleThreadExecutor();

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

        if (newState.isSolid()) {
            blockMap.put(updatedPos, newState.getBlock());
        } else if (oldState.isSolid()) {
            blockMap.remove(updatedPos);
        }
    }

    public void addChunk(Chunk chunk) {
        executorService.submit(() -> {
            for (int x = 0; x < 16; x++) {
                for (int y = 0; y < 256; y++) {
                    for (int z = 0; z < 16; z++) {
                        BlockPos pos = new BlockPos(chunk.getPos().getStartX() + x, y, chunk.getPos().getStartZ() + z);
                        BlockState blockState = chunk.getBlockState(pos);
                        if (blockState.isSolid()) {
                            blockMap.put(pos, blockState.getBlock());
                        }
                    }
                }
            }
        });
    }

    public void removeChunk(Chunk chunk) {
        executorService.submit(() -> {
            for (int x = 0; x < 16; x++) {
                for (int y = 0; y < 256; y++) {
                    for (int z = 0; z < 16; z++) {
                        BlockPos pos = new BlockPos(chunk.getPos().getStartX() + x, y, chunk.getPos().getStartZ() + z);
                        blockMap.remove(pos);
                    }
                }
            }
        });
    }

    public synchronized boolean isBlockAt(BlockPos pos) {
        return blockMap.containsKey(pos);
    }

    public synchronized Block getBlockType(BlockPos pos) {
        return blockMap.get(pos);
    }

    public synchronized boolean removeBlock(BlockPos pos) {
        return blockMap.remove(pos) != null;
    }
}
