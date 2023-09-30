package AutoplayAddon.modules;
import AutoplayAddon.AutoPlay.Movement.Movement;
import AutoplayAddon.AutoPlay.Other.FastBox;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.KeybindSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.misc.Keybind;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import AutoplayAddon.AutoplayAddon;

import java.util.ArrayList;
import java.util.List;

public class CollisionRender extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    public CollisionRender() {
        super(AutoplayAddon.autoplay, "collision-render", "module used for testing");
    }
    List <BlockPos> blockPosList = new ArrayList<>();

    private final Setting<Boolean> renderTrap = sgGeneral.add(new BoolSetting.Builder()
        .name("render-trap")
        .description("Renders a block overlay where you will be teleported.")
        .defaultValue(true)
        .build()
    );


    private final Setting<Boolean> renderPath = sgGeneral.add(new BoolSetting.Builder()
        .name("render-path")
        .description("Renders a block overlay where you will be teleported.")
        .defaultValue(true)
        .build()
    );


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
                Movement.fastBoxList.add(new FastBox(entity));
            }
        })
        .build()
    );



    @EventHandler
    private void onTick(TickEvent.Post event) {
        if (!renderTrap.get()) return;
        List<BlockPos> tempBlocPosList = new ArrayList<>();
        blockPosList.clear();

        for (Entity entity : mc.world.getEntities()) {
            FastBox fastBox = new FastBox(entity);
            List<BlockPos> collidedBlocks = fastBox.getOccupiedBlockPos();

            int minX = Integer.MAX_VALUE;
            int maxX = Integer.MIN_VALUE;
            int minY = Integer.MAX_VALUE;
            int maxY = Integer.MIN_VALUE;
            int minZ = Integer.MAX_VALUE;
            int maxZ = Integer.MIN_VALUE;

            for (BlockPos pos : collidedBlocks) {
                minX = Math.min(minX, pos.getX());
                maxX = Math.max(maxX, pos.getX());
                minY = Math.min(minY, pos.getY());
                maxY = Math.max(maxY, pos.getY());
                minZ = Math.min(minZ, pos.getZ());
                maxZ = Math.max(maxZ, pos.getZ());
            }

            for (BlockPos blockPos : collidedBlocks) {
                for (Direction dir : Direction.values()) {
                    BlockPos offsetPos = blockPos.offset(dir);
                    if (!collidedBlocks.contains(offsetPos)) {
                        tempBlocPosList.add(offsetPos);
                    }
                }
            }

            // Cover the top and bottom extremes
            for (int x = minX; x <= maxX; x++) {
                for (int z = minZ; z <= maxZ; z++) {
                    tempBlocPosList.add(new BlockPos(x, minY - 1, z));
                    tempBlocPosList.add(new BlockPos(x, maxY + 1, z));
                }
            }


        }
        blockPosList.addAll(tempBlocPosList);
    }


    @EventHandler
    private void onRender3D(Render3DEvent event) {
        if (renderTrap.get() && !blockPosList.isEmpty()) {
            for (BlockPos pos : blockPosList) {
                event.renderer.box(
                    pos.getX(), pos.getY(), pos.getZ(),
                    pos.getX() + 1, pos.getY() + 1, pos.getZ() + 1,
                    Color.CYAN, Color.BLUE, ShapeMode.Lines, 0
                );
            }
        }
        if (renderPath.get()) {
            try {
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
            } catch (Exception e) {
                //("error in collision render " + e);
            }

        }
    }
}
