package AutoplayAddon.AutoPlay.Movement;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.orbit.EventHandler;
import static meteordevelopment.meteorclient.MeteorClient.mc;
import net.minecraft.util.math.Vec3d;

public class GotoUtil {
    private static List<GotoUtil> activeInstances = new ArrayList<>();
    private double y;

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (mc.player == null) {
            System.out.println("borked");
            MeteorClient.EVENT_BUS.unsubscribe(this);
            stopAllInstances();
            return;
        }

        if (mc.player.getY() != this.y) {
            MoveToUtil.moveTo(mc.player.getX(), this.y, mc.player.getZ(), false, true);
            return;
        }

        if ((mc.player.getX() != xpos) || (mc.player.getZ() != zpos)) {
            MoveToUtil.moveTo(xpos, y, zpos, false, true);
            return;
        }

        if (mc.player.getY() != ypos) {
            MoveToUtil.moveTo(xpos, ypos, zpos, false, true);
        }

        tickEventFuture.complete(null);
    }

    private static CompletableFuture<Void> tickEventFuture;
    private double xpos;
    private double ypos;
    private double zpos;

    public void moveto(double xpos, double ypos, double zpos) {
        stopAllInstances();
        activeInstances.add(this);

        this.xpos = xpos;
        this.ypos = ypos;
        this.zpos = zpos;
        Vec3d goto1 = new Vec3d(xpos, ypos, zpos);
        ChatUtils.info("Going to " + xpos + " " + ypos + " " + zpos);
        this.y = CanTeleport.searchGoodYandTeleport(mc.player.getPos(), goto1);

        if (this.y == mc.player.getY()) {
            MoveToUtil.moveTo(xpos, ypos, zpos, false, true);
            return;
        }

        MeteorClient.EVENT_BUS.subscribe(this);
        tickEventFuture = new CompletableFuture<>();

        mc.player.setNoGravity(true);
        mc.player.setVelocity(Vec3d.ZERO);

        try {
            tickEventFuture.get();
        } catch (InterruptedException | ExecutionException e) {
            ChatUtils.error("Movement interrupted: " + e.getMessage());
            return;
        }
        mc.player.setNoGravity(false);
        MeteorClient.EVENT_BUS.unsubscribe(this);
        activeInstances.remove(this);
    }

    private void stop() {
        if (tickEventFuture != null && !tickEventFuture.isDone()) {
            tickEventFuture.completeExceptionally(new InterruptedException("Instance stopped"));
        }
    }

    public static void stopAllInstances() {
        for (GotoUtil instance : activeInstances) {
            System.out.println("Stopping Old instance");
            MeteorClient.EVENT_BUS.unsubscribe(instance);
            instance.stop();
        }
        activeInstances.clear();
    }


}
