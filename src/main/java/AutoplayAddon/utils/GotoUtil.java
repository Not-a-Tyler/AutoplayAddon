package AutoplayAddon.utils;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import net.minecraft.util.math.Vec3d;
import static meteordevelopment.meteorclient.MeteorClient.mc;

public class GotoUtil {
    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (mc.player.getY() > -67) {
            MoveToUtil.moveTo(mc.player.getX(), -67, mc.player.getZ(), bigtp, false);
            return;
        }
        if ((Math.round(mc.player.getX()) != Math.round(xpos) || Math.round(mc.player.getZ()) != Math.round(zpos))) {
            MoveToUtil.moveTo(xpos, -67, zpos, bigtp, false);
            return;
        }
        if (ypos == -67) {
            bigtp = false;
            tickEventFuture.complete(null);
        }

        if (mc.player.getY() != ypos){
            bigtp = false;
            MoveToUtil.moveTo(xpos, ypos, zpos, false, false);
        }
        tickEventFuture.complete(null);
    }


    private static CompletableFuture<Void> tickEventFuture;;

    private double xpos;
    private double ypos;
    private double zpos;
    private boolean bigtp;


    public void moveto(double xpos, double ypos, double zpos) {
        this.xpos = xpos;
        this.ypos = ypos;
        this.zpos = zpos;
        ChatUtils.info("Moving to " + xpos + " " + ypos + " " + zpos);
        Vec3d newPos = new Vec3d(xpos, mc.player.getY(), zpos);
        MeteorClient.EVENT_BUS.subscribe(this);
        //add more than 200block tp here no leaks :)
        tickEventFuture = new CompletableFuture<>();
        try {
            tickEventFuture.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        MeteorClient.EVENT_BUS.unsubscribe(this);
    }
}
