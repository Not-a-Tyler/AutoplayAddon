package AutoplayAddon.AutoPlay.Movement;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import AutoplayAddon.Tracker.ServerSideValues;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.orbit.EventPriority;
import net.minecraft.util.math.Vec3d;
import meteordevelopment.meteorclient.mixininterface.IPlayerMoveC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import AutoplayAddon.AutoplayAddon;
import static meteordevelopment.meteorclient.MeteorClient.mc;
public class GotoUtil {
    private static Thread currentMovementThread;

    static CompletableFuture<Void>  tickEventFuture;
    static Vec3d to;

    static double y;
    static boolean postTickFlag;

    static int stage;

    private static Vec3d getStage(Vec3d from, Vec3d to, int stage) {
        if (stage == 1) return new Vec3d(from.getX(), y, from.getZ());
        if (stage == 2) return new Vec3d(to.getX(), y, to.getZ());
        if (stage == 3) return new Vec3d(to.getX(), to.getY(), to.getZ());
        return new Vec3d(from.getX(), from.getY(), from.getZ());
    }

    private static boolean stage() {
        while (mc.player != null) {
            if (stage == 4) {
               // ChatUtils.info("Finished via stage 4");
                return true;
            }
            if (CanTeleport.check(mc.player.getPos(), to) && MovementUtils.predictifPossible(to)) {
                MoveToUtil.moveTo(to);
               // ChatUtils.info("Directly teleported");
                return true;
            }
            Vec3d newPos = getStage(mc.player.getPos(), to, stage);
            if (!MovementUtils.predictifPossible(newPos)) {
                //ChatUtils.info("Not enough charge, waiting 1 tick Allowed: " + AutoplayAddon.values.allowedPlayerTicks + " i: " + AutoplayAddon.values.allowedPlayerTicks + " Delta: " + ServerSideValues.delta());
                return false;
            }
            MoveToUtil.moveTo(newPos);
            stage++;
            //ChatUtils.info("Increased Stage to " + stage);
        }
        MeteorClient.EVENT_BUS.unsubscribe(GotoUtil.class);
        currentMovementThread.interrupt();
        return true;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    private static void onSendPacket(PacketEvent.Send event) {
        //ChatUtils.info("Packet sent");
        if (event.packet instanceof PlayerMoveC2SPacket && ((IPlayerMoveC2SPacket) event.packet).getTag() != 13377) event.cancel();
    }


    @EventHandler()
    private static void onPreTick(TickEvent.Pre event) {
        //ChatUtils.info("Tick started");
        if (postTickFlag) {
            postTickFlag = false; // Reset the flag for the next post tick
            //ChatUtils.info("Tick started");
            if (stage()) {
                MeteorClient.EVENT_BUS.unsubscribe(GotoUtil.class);
                tickEventFuture.complete(null);
                //ChatUtils.info("stage complete");
            } else {
                //ChatUtils.info("stage finished");
            }
        }
    }


    @EventHandler()
    private static void onPostTick(TickEvent.Pre event) {
        postTickFlag = true;
    }

    public static void moveto(double xpos, double ypos, double zpos, Boolean wait) {
        if (currentMovementThread != null && currentMovementThread.isAlive()) {
            currentMovementThread.interrupt();
        }
        currentMovementThread = new Thread(() -> {
            stage = 1;
            postTickFlag = true;
            to = new Vec3d(xpos, ypos, zpos);
            y = CanTeleport.searchY(mc.player.getPos(), to);
            //ChatUtils.info("Going to " + xpos + " " + ypos + " " + zpos + " with Y: " + y);
            //ChatUtils.sendPlayerMsg("Going to " + xpos + " " + ypos + " " + zpos + " with Y: " + y);
            tickEventFuture = new CompletableFuture<>();
            mc.player.setNoGravity(true);
            mc.player.setVelocity(Vec3d.ZERO);
            MeteorClient.EVENT_BUS.subscribe(GotoUtil.class);
            try {
                tickEventFuture.get();
            } catch (InterruptedException | ExecutionException e) {
                //ChatUtils.error("Movement interrupted: " + e.getMessage());
                return;
            }
            //MeteorClient.EVENT_BUS.unsubscribe(GotoUtil.class);
            if (mc.player != null) mc.player.setNoGravity(false);
            //ChatUtils.info("Finished");
            //ChatUtils.sendPlayerMsg("Finished");
        });
        currentMovementThread.start();
        if (wait) {
            try {
                currentMovementThread.join();
            } catch (InterruptedException e) {
                //ChatUtils.error("Movement interrupted: " + e.getMessage());
            }
        }
    }
}
