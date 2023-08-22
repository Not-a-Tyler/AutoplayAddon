package AutoplayAddon.modules;
import AutoplayAddon.AutoPlay.Movement.MoveToUtil;
import AutoplayAddon.AutoPlay.Movement.Movement;
import AutoplayAddon.AutoplayAddon;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import net.minecraft.util.math.BlockPos;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.util.math.Vec3d;


public class LongDistanceTest extends Module {

    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    public Setting<BlockPos> pos = sgGeneral.add(new BlockPosSetting.Builder()
        .name("Destination")
        .description("The to position.")
        .defaultValue(BlockPos.ORIGIN)
        .build()
    );

    private final Setting<Integer> ticksToSkip = sgGeneral.add(new IntSetting.Builder()
        .name("Ticks to skip")
        .description("How many ticks to wait betyween each teleport.")
        .defaultValue(0)
        .min(0)
        .sliderMax(100)
        .build());


    public LongDistanceTest() {
        super(AutoplayAddon.autoplay, "long-tp", "Goes to somewhere");
    }
    int tickCounter = 0;
    @EventHandler
    private void onTick(TickEvent.Post event) {
        double playerY = mc.player.getY();
        if ((playerY != -66) || (playerY != 322)) {
            double targetValue1 = -66;
            double targetValue2 = 322;

            double differenceToValue1 = Math.abs(playerY - targetValue1);
            double differenceToValue2 = Math.abs(playerY - targetValue2);
            Vec3d pos;
            if (differenceToValue1 < differenceToValue2) {
                pos = new Vec3d(mc.player.getX(), -66, mc.player.getZ());
            } else {
                pos = new Vec3d(mc.player.getX(), 322, mc.player.getZ());
            }
            Movement.moveTo(pos);
            return;
        }
        tickCounter++;
        if (tickCounter < ticksToSkip.get()) {
            return;
        }
        tickCounter = 0; // Reset the tick counter
        Vec3d playerPos = mc.player.getPos();

        Vec3d destination = new Vec3d(pos.get().getX(), pos.get().getY(), pos.get().getZ());
        Vec3d direction = destination.subtract(playerPos).normalize(); // Step 1
        double step = 16.0; // Step size: one chunk at a time

        Vec3d currentPos = playerPos;
        BlockPos lastLoadedPos = null;
        double maxDistance = Math.min(1000, currentPos.distanceTo(destination));
        while (currentPos.distanceTo(playerPos) < maxDistance) { // Step 3
            currentPos = currentPos.add(direction.multiply(step)); // Step 2
            BlockPos blockPos = new BlockPos((int) currentPos.x, (int) currentPos.y, (int) currentPos.z);
            assert mc.world != null;
            if (mc.world.isChunkLoaded(blockPos.getX() >> 4, blockPos.getZ() >> 4)) { // Step 4
                lastLoadedPos = blockPos;
            } else {
                break;
            }
        }

        // If lastLoadedPos is null, that means no loaded chunks were found in the direction of the destination
        if (lastLoadedPos == null) {
            ChatUtils.info("No loaded chunks found in the direction of the destination.");
            return;
        }

        ChatUtils.info("Teleporting to " + lastLoadedPos.getX() + " " + lastLoadedPos.getY() + " " + lastLoadedPos.getZ());
        Movement.moveTo(lastLoadedPos.toCenterPos());
    }



}
