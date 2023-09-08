package AutoplayAddon.modules;
import AutoplayAddon.AutoPlay.Movement.Movement;
import AutoplayAddon.AutoPlay.Other.FastBox;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.settings.KeybindSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.misc.Keybind;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.util.math.Vec3d;
import AutoplayAddon.AutoplayAddon;

public class CollisionRender extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    public CollisionRender() {
        super(AutoplayAddon.autoplay, "collision-render", "module used for testing");
    }


    private final Setting<Keybind> cancelBlink = sgGeneral.add(new KeybindSetting.Builder()
        .name("Keybind to tp")
        .description("Cancels sending packets and sends you back to your original position.")
        .defaultValue(Keybind.none())
        .action(() -> {
            Movement.fastBoxList.clear();
            Movement.fastBoxBadList.clear();
        })
        .build()
    );




    int frameCounter = 0; // Initialize a frame counter variable
    @EventHandler
    private void onRender3D(Render3DEvent event) {


        for (FastBox fastBox : Movement.fastBoxList) {
            for (Vec3d corner1 : fastBox.corners) {
                for (Vec3d corner2 : fastBox.corners) {
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
