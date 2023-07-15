package AutoplayAddon.AutoPlay.Movement;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import static meteordevelopment.meteorclient.MeteorClient.mc;
import net.minecraft.util.math.Vec3d;

public class GotoUtil {
    private double y;

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (mc.player == null) {
            MeteorClient.EVENT_BUS.unsubscribe(this);
            tickEventFuture.complete(null);
        }
        mc.player.setNoGravity(true);
        mc.player.setVelocity(Vec3d.ZERO);
        if (mc.player.getY() != this.y) {
            //ChatUtils.info("dosent equal y");
            MoveToUtil.moveTo(mc.player.getX(), this.y, mc.player.getZ(), false, true);
            return;
        }
        if ((mc.player.getX() != xpos) || (mc.player.getZ() != zpos) ) {
            //ChatUtils.info("dosent equal x/z");
            MoveToUtil.moveTo(xpos, y, zpos, false, true);
            return;
        }

        if (mc.player.getY() != ypos) {
            MoveToUtil.moveTo(xpos, ypos, zpos, false, true);
        }
        mc.player.setNoGravity(false);
        tickEventFuture.complete(null);

    }

    private static CompletableFuture<Void> tickEventFuture;

    private double xpos;
    private double ypos;
    private double zpos;

    public void moveto(double xpos, double ypos, double zpos) {
        this.xpos = xpos;
        this.ypos = ypos;
        this.zpos = zpos;
        Vec3d goto1 = new Vec3d(xpos, ypos, zpos);
        ChatUtils.info("Moving to " + xpos + " " + ypos + " " + zpos);
        this.y = CanTeleport.searchGoodYandTeleport(mc.player.getPos(), goto1);
        if (this.y == mc.player.getY()) {
            ChatUtils.info("no need to advanced, using simple");
            MoveToUtil.moveTo(xpos, ypos, zpos, false, true);
            return;
        }

        MeteorClient.EVENT_BUS.subscribe(this);
        tickEventFuture = new CompletableFuture<>();
        try {
            tickEventFuture.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }

        MeteorClient.EVENT_BUS.unsubscribe(this);
    }


}
