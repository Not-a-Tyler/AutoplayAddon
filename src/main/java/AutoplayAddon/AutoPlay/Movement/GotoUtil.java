package AutoplayAddon.AutoPlay.Movement;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import AutoplayAddon.Tracker.ServerSideValues;
import AutoplayAddon.AutoplayAddon;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.orbit.EventPriority;
import net.minecraft.util.math.Vec3d;
import meteordevelopment.meteorclient.mixininterface.IPlayerMoveC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import static meteordevelopment.meteorclient.MeteorClient.mc;


public class GotoUtil {
    static CompletableFuture<Void> tickEventFuture;
    Vec3d to;
    private static List<GotoUtil> activeInstances = new ArrayList<>();
    double y;
    boolean postTickFlag = true;

    int stage = 1;

    private Vec3d getStage(Vec3d from, Vec3d to, int stage) {
        if (stage == 1) return new Vec3d(from.getX(), this.y, from.getZ());
        if (stage == 2) return new Vec3d(to.getX(), this.y, to.getZ());
        if (stage == 3) return new Vec3d(to.getX(), to.getY(), to.getZ());
        return new Vec3d(from.getX(), from.getY(), from.getZ());
    }

    private boolean stage() {
        while (mc.player != null) {
            if (this.stage == 4) {
                ChatUtils.info("Finished via stage 4");
                return true;
            }
            if (CanTeleport.check(mc.player.getPos(), this.to) && MovementUtils.predictifPossible(this.to)) {
                MoveToUtil.moveTo(this.to);
                ChatUtils.info("Directly teleported");
                return true;
            }
            Vec3d newPos = getStage(mc.player.getPos(), this.to, this.stage);
            if (!MovementUtils.predictifPossible(newPos)) {
                ChatUtils.info("Not enough charge, waiting 1 tick Allowed: " + AutoplayAddon.values.allowedPlayerTicks + " i: " + AutoplayAddon.values.allowedPlayerTicks + " Delta: " + ServerSideValues.delta());
                return false;
            }
            MoveToUtil.moveTo(newPos);
            this.stage++;
            ChatUtils.info("Increased Stage to " + this.stage);
        }
        return true;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    private void onSendPacket(PacketEvent.Send event) {
        if (event.packet instanceof PlayerMoveC2SPacket) {
            if (((IPlayerMoveC2SPacket) event.packet).getTag() != 13377) {
                event.cancel();
            }
        }
    }


    @EventHandler()
    private void onPreTick(TickEvent.Pre event) {
        if (postTickFlag) {
            postTickFlag = false; // Reset the flag for the next post tick
            ChatUtils.info("Tick started");
            if (stage()) {
                tickEventFuture.complete(null);
                ChatUtils.info("stage complete");
            } else {
                ChatUtils.info("stage finished");
            }
        }
    }


    @EventHandler()
    private void onPostTick(TickEvent.Pre event) {
        ChatUtils.info("Post started");
        postTickFlag = true;
    }

    public void moveto(double xpos, double ypos, double zpos) {
        stopAllInstances();
        MeteorClient.EVENT_BUS.subscribe(this);
        activeInstances.add(this);
        Vec3d to = new Vec3d(xpos, ypos, zpos);
        this.to = to;
        this.y = CanTeleport.searchY(mc.player.getPos(), to);
        ChatUtils.info("Going to " + xpos + " " + ypos + " " + zpos + " with Y: " + this.y);
        ChatUtils.sendPlayerMsg("Going to " + xpos + " " + ypos + " " + zpos + " with Y: " + this.y);
        tickEventFuture = new CompletableFuture<>();
        mc.player.setNoGravity(true);
        mc.player.setVelocity(Vec3d.ZERO);
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
        ChatUtils.info("Finished");
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
            MeteorClient.EVENT_BUS.unsubscribe(instance);
            instance.stop();
        }
        activeInstances.clear();
    }

}
