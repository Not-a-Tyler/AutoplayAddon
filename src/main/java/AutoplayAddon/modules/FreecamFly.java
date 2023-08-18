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
        AIDS.init(false);
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
        if (!Movement.AIDSboolean) {
            AIDS.disable();
            toggle();
        }
        Vec3d cameraPos = mc.gameRenderer.getCamera().getPos();
        double playerEyeHeight = mc.player.getEyeHeight(mc.player.getPose());
        mc.player.setPosition(new Vec3d(cameraPos.x, -66, cameraPos.z));
        Vec3d playerPos = new Vec3d(cameraPos.x, cameraPos.y - mc.player.getEyeHeight(mc.player.getPose()), cameraPos.z);
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
