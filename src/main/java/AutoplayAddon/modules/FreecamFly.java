package AutoplayAddon.modules;
import AutoplayAddon.AutoPlay.Movement.AIDS;
import AutoplayAddon.AutoPlay.Movement.Movement;
import AutoplayAddon.AutoplayAddon;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.systems.modules.render.Freecam;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.orbit.EventPriority;
import net.minecraft.client.render.Camera;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;


public class FreecamFly extends Module {
    public FreecamFly() {
        super(AutoplayAddon.autoplay, "freecam-fly", "Example");
    }

    @Override
    public void onActivate() {
        AIDS.init();
        Module freecam = Modules.get().get(Freecam.class);
        if (!freecam.isActive()) {
            freecam.toggle();
        }
    }

    @Override
    public void onDeactivate() {
        Camera camera = mc.gameRenderer.getCamera();
        AIDS.disable();
        Module freecam = Modules.get().get(Freecam.class);
        if (freecam.isActive()) {
            freecam.toggle();
        }
        mc.player.setPitch(camera.getPitch());
        mc.player.setYaw(camera.getYaw());
    }

    @EventHandler(priority = EventPriority.HIGHEST + 3)
    private void onTick(TickEvent.Pre event) {

        Vec3d cameraPos = mc.gameRenderer.getCamera().getPos();
        double playerEyeHeight = mc.player.getEyeHeight(mc.player.getPose()); // Assuming there's a method for this, you can adjust accordingly.

        Vec3d playerPos = new Vec3d(
            cameraPos.x,
            cameraPos.y - playerEyeHeight,
            cameraPos.z
        );

        Box box = new Box(
            playerPos.x - mc.player.getWidth() / 2,
            playerPos.y,
            playerPos.z - mc.player.getWidth() / 2,
            playerPos.x + mc.player.getWidth() / 2,
            playerPos.y + mc.player.getHeight(),
            playerPos.z + mc.player.getWidth() / 2
        );

        if (mc.world.isSpaceEmpty(box)) {
            Movement.moveTo(playerPos);
        }
    }


}
