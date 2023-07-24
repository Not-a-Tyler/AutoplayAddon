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

    @EventHandler()
    private void onTick(TickEvent.Pre event) {
        if (mc.player == null) {
            System.out.println("borked");
            tickEventFuture.complete(null);
            return;
        }
        while (true) {
            if (stage == 4) {
                tickEventFuture.complete(null);
                break;
            }
            Vec3d newPos = getStage(mc.player.getPos(), this.to, stage);
            ChatUtils.info(ServerSideValues.delta() + " > " + ((int) Math.ceil(PlayerUtils.distanceTo(this.to) / 10.0)));
            if(!AutoplayAddon.values.moveable) {
                if (ServerSideValues.delta() < ((int) Math.ceil(PlayerUtils.distanceTo(this.to) / 10.0))){
                    ChatUtils.sendPlayerMsg("Not enough charge, waiting 1 tick");
                    return;
                }
            }
            MoveToUtil.moveTo(newPos);
            ChatUtils.info(("Increased Stage"));
            stage++;
        }
    }

    private static CompletableFuture<Void> tickEventFuture;
    private Vec3d to;


    public void moveto(double xpos, double ypos, double zpos) {
        stopAllInstances();
        activeInstances.add(this);
        Vec3d to = new Vec3d(xpos, ypos, zpos);
        this.to = to;
        ChatUtils.info("Going to " + xpos + " " + ypos + " " + zpos);
        ChatUtils.sendPlayerMsg("Going to " + xpos + " " + ypos + " " + zpos);
        this.y = CanTeleport.searchGoodYandTeleport(mc.player.getPos(), to);
        tickEventFuture = new CompletableFuture<>();
        mc.player.setNoGravity(true);
        mc.player.setVelocity(Vec3d.ZERO);
        MeteorClient.EVENT_BUS.subscribe(this);
        try {
            tickEventFuture.get();
        } catch (InterruptedException | ExecutionException e) {
            ChatUtils.error("Movement interrupted: " + e.getMessage());
            return;
        }
        MeteorClient.EVENT_BUS.unsubscribe(this);
        if (mc.player != null) {
            mc.player.setNoGravity(false);
        }
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
