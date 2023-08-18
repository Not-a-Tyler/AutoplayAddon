package AutoplayAddon.AutoPlay.Movement;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.util.math.Vec3d;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static meteordevelopment.meteorclient.MeteorClient.mc;

public class AIDS extends Movement {
    public static Thread currentAIDSmoveToThread;
    static CompletableFuture<Void>  tickEventFuture;
    static boolean AutoSetPosition;
    public static void init(Boolean automaticallySetPosition) {
        currentPosition = mc.player.getPos();
        MeteorClient.EVENT_BUS.subscribe(AIDS.class);
        MeteorClient.EVENT_BUS.subscribe(Movement.class);
        mc.player.setNoGravity(true);
        mc.player.setVelocity(Vec3d.ZERO);
        to = mc.player.getPos();
        AIDSboolean = true;
        AutoSetPosition = automaticallySetPosition;
    }
    public static void disable() {
        MeteorClient.EVENT_BUS.unsubscribe(AIDS.class);
        MeteorClient.EVENT_BUS.unsubscribe(Movement.class);
        if (currentAIDSmoveToThread != null && currentAIDSmoveToThread.isAlive()) {
            currentAIDSmoveToThread.interrupt();
        }
        if (mc.player != null) mc.player.setNoGravity(false);
        AIDSboolean = false;
    }

    public static void setPos(Vec3d pos) {
        to = pos;
        tickEventFuture = new CompletableFuture<>();
        AIDSmoveTo();
        try {
            tickEventFuture.get();
        //    ChatUtils.info("Finished moving to " + to.toString());
        } catch (InterruptedException | ExecutionException e) {
            //ChatUtils.error("AIDSmoveTo Movement interrupted: " + e.getMessage());
        }
    }


    @EventHandler()
    private static void onPostTick(TickEvent.Post event) {
        if (mc.player == null) {
            disable();
            return;
        }
        Boolean setPos = false;
        if (!setPos && !closeBy(currentPosition, to) && (currentAIDSmoveToThread == null || !currentAIDSmoveToThread.isAlive())) {
            ChatUtils.info("moving due to not being there");
            AIDSmoveTo();
        }

        if (!closeBy(mc.player.getPos(), to) && AutoSetPosition) {
            mc.player.setPosition(to);
        }
    }

    public static boolean closeBy(Vec3d from, Vec3d to) {
        double dx = from.x - to.x;
        double dy = from.y - to.y;
        double dz = from.z - to.z;
        double squaredDistance = dx * dx + dy * dy + dz * dz;
        return squaredDistance < 0.01;
    }


    public static void AIDSmoveTo() {
        if (!closeBy(currentPosition, to)) {
            if (currentAIDSmoveToThread != null && currentAIDSmoveToThread.isAlive()) {
                currentAIDSmoveToThread.interrupt();
                //ChatUtils.error("Interrupted previous AIDSmoveTo thread");
            }
            currentAIDSmoveToThread = new Thread(() -> {
                GotoUtil.shortGoTo();
                tickEventFuture.complete(null);
            });
            currentAIDSmoveToThread.start();
        }
    }
}
