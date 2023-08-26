package AutoplayAddon.AutoPlay.Movement;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.util.math.Vec3d;
import static meteordevelopment.meteorclient.MeteorClient.mc;
public class GotoUtil extends Movement {
    static CompletableFuture<Void>  tickEventFuture;
    static double y;

    static int stage;

    private static Vec3d getStage(Vec3d from, Vec3d to, int stage) {
        if (stage == 1) return new Vec3d(from.getX(), y, from.getZ());
        if (stage == 2) return new Vec3d(to.getX(), y, to.getZ());
        if (stage == 3) return new Vec3d(to.getX(), to.getY(), to.getZ());
        return new Vec3d(from.getX(), from.getY(), from.getZ());
    }

    private static boolean stage() {
        while (mc.player != null) {
          //  ChatUtils.info("stage starting" + System.currentTimeMillis());
          //  ChatUtils.info("stage started");
            if (stage == 4) {
              //  ChatUtils.info("Finished via stage 4");
                return true;
            }
            if (CanTeleport.oldCheck(currentPosition, to) && predictifPossible(to)) {
                MoveToUtil.moveTo(to);
             //   ChatUtils.info("Directly teleported");
                return true;
            }
            Vec3d newPos = getStage(currentPosition, to, stage);
            if (!predictifPossible(newPos)) {
              //  ChatUtils.info("stage ended: Not enough charge, waiting 1 tick");
                return false;
            }
            MoveToUtil.moveTo(newPos);
       //     ChatUtils.info("increasing stage");
            stage++;
      //      ChatUtils.info("stage increasing" + System.currentTimeMillis());
        }
        MeteorClient.EVENT_BUS.unsubscribe(GotoUtil.class);
        return true;
    }


    @EventHandler()
    private static void onPreTick(TickEvent.Pre event) {
        if (stage()) {
            MeteorClient.EVENT_BUS.unsubscribe(GotoUtil.class);
            tickEventFuture.complete(null);
        }
    }


    public static void shortGoTo() {
        y = CanTeleport.searchY(currentPosition, to);
        if (y == -1337) {
            y = currentPosition.y;
            stage = 2;
        } else {
            stage = 1;
        }
        tickEventFuture = new CompletableFuture<>();
       // ChatUtils.sendPlayerMsg("going to " + to);
       // ChatUtils.info("going to " + to + " current time " + System.currentTimeMillis());
        if (!stage()) {
          //  ChatUtils.info("subscribing");
            MeteorClient.EVENT_BUS.subscribe(GotoUtil.class);
            try {
                tickEventFuture.get();
            } catch (InterruptedException | ExecutionException e) {
                ChatUtils.info("goto utils interupped " + e);
            }
        }
       // ChatUtils.info("goto util complete" + " current time " + System.currentTimeMillis());
       // ChatUtils.sendPlayerMsg("goto util complete");
    }
}
