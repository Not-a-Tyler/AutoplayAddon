package AutoplayAddon.AutoPlay.Movement;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.util.math.Vec3d;
import static meteordevelopment.meteorclient.MeteorClient.mc;

public class GotoUtilReference extends Movement {
    static double y;
    static CompletableFuture<Void>  tickEventFuture;
    static Vec3d to;
    static int stage;
    public static Boolean currentlyMoving = false;
    private static int waitTicks = 0;


    private static Vec3d getStage(Vec3d from, Vec3d to, int stage) {
        if (stage == 1) return new Vec3d(from.getX(), y, from.getZ());
        if (stage == 2) return new Vec3d(to.getX(), y, to.getZ());
        if (stage == 3) return new Vec3d(to.getX(), to.getY(), to.getZ());
        return new Vec3d(from.getX(), from.getY(), from.getZ());
    }

    private static boolean stage() {
        while (mc.player != null) {
           // ChatUtils.info("Stage Starting: " + stage);
            if (stage == 4) {
              //  ChatUtils.error("finished via stage 4");
                return true;
            }
            if (CanTeleport.lazyCheck(currentPosition, to)) {
                if (predictifPossible(to, "direct teleport")) {
                //    ChatUtils.info("Directly teleporting");
                    MoveToUtil.moveTo(to);
                    return true;
                } else {
                    MoveToUtil.sendAllPacketsFromQueue();
                  //  ChatUtils.info("Waiting 3 ticks and directly teleporting");
                    waitTicks = 3;
                    return false;
                }
            }
            Vec3d newPos = getStage(currentPosition, to, stage);
            if (!predictifPossible(newPos, "next stage")) {
                MoveToUtil.sendAllPacketsFromQueue();
          //      ChatUtils.info("Waiting 3 ticks and staying on stage");
                waitTicks = 3;
                return false;
            }
            MoveToUtil.moveTo(newPos);
            stage++;
        }
        disable();
        return true;
    }

    @EventHandler()
    private static void onPreTick(TickEvent.Pre event) {
        if (mc.player == null) return;
        if (AutoSetPosition && !closeBy(mc.player.getPos(), currentPosition)) {
            mc.player.setPosition(to);
        }
        if (waitTicks > 0) {
            waitTicks--;
            return;
        }

        if (!currentlyMoving) return;
     //   ChatUtils.info("Tick");
        if (stage()) {
            currentlyMoving = false;
         //   ChatUtils.info("completed");
            tickEventFuture.complete(null);
        }
    }


    public static void setPos(Vec3d pos) {
        if (autoSendPackets) {
            MoveToUtil.packetQueue.clear();
        }
        to = pos;
        if (closeBy(currentPosition, to)) return;
      //  mc.player.networkHandler.sendChatMessage("Teleporting to " + pos);
//        ChatUtils.info("");
//        ChatUtils.info("");
//        ChatUtils.info("Teleporting to " + pos);
        y = Path.searchY(currentPosition, to);
        if (y == -1337) {
            y = currentPosition.y;
            stage = 2;
        } else {
            stage = 1;
        }
        tickEventFuture = new CompletableFuture<>();
        if (!stage()) {
            currentlyMoving = true;
         //   ChatUtils.info("subscribing");
            try {
                tickEventFuture.get();
            } catch (InterruptedException | ExecutionException e) {
                ChatUtils.error("goto utils interupped " + e);
            }
        }
        if (autoSendPackets) {
            MoveToUtil.sendAllPacketsFromQueue();
        }
        //mc.player.networkHandler.sendChatMessage("Finished teleporting to " + pos);
//        ChatUtils.info("finished setting pos");
//        ChatUtils.info("");
//        ChatUtils.info("");
    }
}
