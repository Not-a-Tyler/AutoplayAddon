package AutoplayAddon.AutoPlay.Movement;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import AutoplayAddon.AutoplayAddon;
import AutoplayAddon.Tracker.ServerSideValues;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.meteorclient.utils.player.PlayerUtils;
import meteordevelopment.orbit.EventHandler;
import static meteordevelopment.meteorclient.MeteorClient.mc;

import meteordevelopment.orbit.EventPriority;
import net.minecraft.util.math.Vec3d;

public class GotoUtil {
    private static List<GotoUtil> activeInstances = new ArrayList<>();
    private double y;
    int stage = 1;

    private Vec3d getStage(Vec3d from, Vec3d to, int stage) {
        if (stage == 1) return new Vec3d(from.getX(), this.y, from.getZ());
        if (stage == 2) return new Vec3d(to.getX(), this.y, to.getZ());
        if (stage == 3) return new Vec3d(to.getX(), to.getY(), to.getZ());
        return new Vec3d(from.getX(), from.getY(), from.getZ());
    }

    private double predictPacketsTo(Vec3d newPos) {
        int predict;
        double base = ServerSideValues.findFarthestDistance(newPos);
        int packetsRequired = (int) Math.floor(Math.abs(base / 10.0));
        if (AutoplayAddon.values.hasMoved) {
            predict = ((packetsRequired + 1) * 2);
            ChatUtils.info("Predicted " + (ServerSideValues.delta() - predict) + " since player has moved");
        } else {
            predict = (packetsRequired + 2);
            ChatUtils.info("Predicted " + (ServerSideValues.delta() - predict) + " since player has not moved");
        }
        return predict;
    }

    private boolean stage() {
        while (mc.player != null) {
            if (this.stage == 4) {
                ChatUtils.info("Finished via stage 4");
                return true;
            }
            if (!CanTeleport.Checkifcantteleport(mc.player.getPos(), this.to)) {
                if (ServerSideValues.delta() > predictPacketsTo(this.to)) {
                    MoveToUtil.moveTo(this.to);
                    ChatUtils.info("Directly teleported");
                    return true;
                }
            }
            Vec3d newPos = getStage(mc.player.getPos(), this.to, this.stage);
            if (ServerSideValues.delta() < predictPacketsTo(newPos)) {
                ChatUtils.info("Not enough charge, waiting 1 tick");
                return false;
            }
            MoveToUtil.moveTo(newPos);
            this.stage++;
            ChatUtils.info("Increased Stage to " + this.stage);
        }
        return true;
    }


    @EventHandler()
    private void onTick(TickEvent.Pre event) {
        ChatUtils.info("Tick started");
        if (mc.player == null) {
            System.out.println("borked");
            tickEventFuture.complete(null);
            return;
        }
        if (stage()) {
            tickEventFuture.complete(null);
        }
    }

    private static CompletableFuture<Void> tickEventFuture;
    private Vec3d to;


    public void moveto(double xpos, double ypos, double zpos) {
        stopAllInstances();
        activeInstances.add(this);
        Vec3d to = new Vec3d(xpos, ypos, zpos);
        this.to = to;
        this.y = CanTeleport.searchGoodYandTeleport(mc.player.getPos(), to);
        ChatUtils.sendPlayerMsg("Going to " + xpos + " " + ypos + " " + zpos + " with Y: " + this.y);
        tickEventFuture = new CompletableFuture<>();
        mc.player.setNoGravity(true);
        mc.player.setVelocity(Vec3d.ZERO);
        if (!stage()) {
            MeteorClient.EVENT_BUS.subscribe(this);
            try {
                tickEventFuture.get();
            } catch (InterruptedException | ExecutionException e) {
                ChatUtils.error("Movement interrupted: " + e.getMessage());
                return;
            }
            MeteorClient.EVENT_BUS.unsubscribe(this);
        }
        if (mc.player != null) {
            mc.player.setNoGravity(false);
        }
        ChatUtils.sendPlayerMsg("Finished");
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
