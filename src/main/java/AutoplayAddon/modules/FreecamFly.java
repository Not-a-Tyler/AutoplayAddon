package AutoplayAddon.modules;
import AutoplayAddon.AutoPlay.Movement.GotoUtil;
import AutoplayAddon.AutoPlay.Movement.Movement;
import AutoplayAddon.AutoPlay.Other.FastBox;
import AutoplayAddon.AutoplayAddon;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.systems.modules.render.Freecam;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.orbit.EventPriority;
import net.minecraft.client.render.Camera;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;
import net.minecraft.util.math.Vec3d;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;


public class FreecamFly extends Module {
    public FreecamFly() {
        super(AutoplayAddon.autoplay, "freecam-fly", "Example");
    }

    @Override
    public void onActivate() {
        Vec3d cameraPos = mc.gameRenderer.getCamera().getPos();
        mc.player.setPos(cameraPos.x, -66, cameraPos.z);
        GotoUtil.init(false, true);
        Module freecam = Modules.get().get(Freecam.class);
        if (!freecam.isActive()) {
            freecam.toggle();
        }
    }


    @Override
    public void onDeactivate() {
        Camera camera = mc.gameRenderer.getCamera();
        Vec3d cameraPos = camera.getPos();
        Vec3d playerPos = new Vec3d(cameraPos.x, cameraPos.y - mc.player.getEyeHeight(mc.player.getPose()), cameraPos.z);
        mc.player.setPos(playerPos.x, playerPos.y, playerPos.z);
        GotoUtil.disable();
        mc.player.setPitch(camera.getPitch());
        mc.player.setYaw(camera.getYaw());
        Module freecam = Modules.get().get(Freecam.class);
        if (freecam.isActive()) {
            freecam.toggle();
        }
    }





    @EventHandler(priority = EventPriority.HIGHEST + 3)
    private void onTick(TickEvent.Pre event) {
        Vec3d cameraPos = mc.gameRenderer.getCamera().getPos();
        mc.player.setPos(cameraPos.x, -66, cameraPos.z);
        Vec3d playerPos = new Vec3d(cameraPos.x, cameraPos.y - mc.player.getEyeHeight(mc.player.getPose()), cameraPos.z);
        FastBox fatsbox = new FastBox(playerPos);

        if (!fatsbox.isCollidingWithBlocks()) {
            Movement.moveTo(playerPos);
        }
    }


}
