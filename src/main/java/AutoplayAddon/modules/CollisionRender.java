package AutoplayAddon.modules;
import AutoplayAddon.AutoPlay.Movement.Movement;
import AutoplayAddon.AutoPlay.Other.FastBox;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.settings.KeybindSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.misc.Keybind;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import AutoplayAddon.AutoplayAddon;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class CollisionRender extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    public CollisionRender() {
        super(AutoplayAddon.autoplay, "collision-render", "module used for testing");
    }
    List <BlockPos> blockPosList = new ArrayList<>();

    private final Setting<Keybind> cancelBlink = sgGeneral.add(new KeybindSetting.Builder()
        .name("Keybind to tp")
        .description("Cancels sending packets and sends you back to your original position.")
        .defaultValue(Keybind.none())
        .action(() -> {
            Movement.fastBoxList.clear();
            Movement.fastBoxBadList.clear();
            blockPosList.clear();
        })
        .build()
    );





    private final Setting<Keybind> allEntities = sgGeneral.add(new KeybindSetting.Builder()
        .name("allentities")
        .description("Cancels sending packets and sends you back to your original position.")
        .defaultValue(Keybind.none())
        .action(() -> {
            //loop through entitiesd
            for (Entity entity : mc.world.getEntities()) {
                Movement.fastBoxList.add(new FastBox(entity.getBoundingBox()));
            }
        })
        .build()
    );



    @EventHandler
    private void onTick(TickEvent.Post event) {
        List<BlockPos> tempBlocPosList = new ArrayList<>();
        blockPosList.clear();

        for (Entity entity : mc.world.getEntities()) {
            FastBox fastBox = new FastBox(entity.getBoundingBox());
            List<BlockPos> collidedBlocks = fastBox.getOccupiedBlockPos();

            // Find the highest and lowest Y values of the collided blocks
            int minY = collidedBlocks.stream().min(Comparator.comparingInt(BlockPos::getY)).orElseThrow().getY();
            int maxY = collidedBlocks.stream().max(Comparator.comparingInt(BlockPos::getY)).orElseThrow().getY();

            // Determine the middle Y position between top and bottom of the entity
            int middleY = minY + (maxY - minY) / 2;

            // Create a ring halfway between top and bottom
            for (BlockPos blockPos : collidedBlocks) {
                if (blockPos.getY() == middleY) {
                    for (Direction dir : Direction.values()) {
                        if (dir != Direction.UP && dir != Direction.DOWN) {
                            BlockPos offsetPos = blockPos.offset(dir);
                            if (!collidedBlocks.contains(offsetPos)) {
                                tempBlocPosList.add(offsetPos);
                            }
                        }
                    }
                }
            }

            // Add blocks above and below the blocks that have the highest and lowest Y-values
            for (BlockPos blockPos : collidedBlocks) {
                if (blockPos.getY() == maxY) {
                    tempBlocPosList.add(blockPos.up());
                }
                if (blockPos.getY() == minY) {
                    tempBlocPosList.add(blockPos.down());
                }
            }
        }
        ChatUtils.info("tempBlocPosList size: " + tempBlocPosList.size());
        blockPosList.addAll(tempBlocPosList);
    }




    int frameCounter = 0; // Initialize a frame counter variable
    @EventHandler
    private void onRender3D(Render3DEvent event) {

        for (BlockPos pos : blockPosList) {
            // Using the provided box example:
            event.renderer.box(
                pos.getX(), pos.getY(), pos.getZ(),
                pos.getX() + 1, pos.getY() + 1, pos.getZ() + 1,
                Color.CYAN, Color.BLUE, ShapeMode.Sides, 0
            );
        }
        for (FastBox fastBox : Movement.fastBoxList) {
            if (fastBox.corners == null) continue;
            for (Vec3d corner1 : fastBox.corners) {
                for (Vec3d corner2 : fastBox.corners) {
                    if (corner1 == null || corner2 == null) continue;
                    event.renderer.line(corner1.x, corner1.y, corner1.z, corner2.x, corner2.y, corner2.z, Color.ORANGE);
                }
            }
        }

        for (FastBox fastBox : Movement.fastBoxBadList) {
            for (Vec3d corner1 : fastBox.corners) {
                for (Vec3d corner2 : fastBox.corners) {
                    event.renderer.line(corner1.x, corner1.y, corner1.z, corner2.x, corner2.y, corner2.z, Color.RED);
                }
            }
        }


        frameCounter++; // Increment the frame counter

        if (frameCounter == 120) {
            int boxCount = Movement.fastBoxList.size(); // Get the count of boxes to render
            System.out.println("Number of boxes to render: " + boxCount); // Print the box count
            frameCounter = 0; // Reset the frame counter
        }
    }




}
