package AutoplayAddon.AutoPlay.Movement;
import java.util.ArrayList;
import java.util.List;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.util.math.Vec3d;
import static meteordevelopment.meteorclient.MeteorClient.mc;



public class GotoQueue extends Movement {
    static Vec3d to;
    public static List<Task> tasks = new ArrayList<>();

    @EventHandler()
    private static void onPreTick(TickEvent.Pre event) {
        if (AutoSetPosition && !closeBy(mc.player.getPos(), currentPosition)) {
            mc.player.setPosition(to);
        }
        if (mc.player == null || tasks.isEmpty()) return;

        Task currentTask = tasks.get(0);
        currentTask.execute();

        if (currentTask.isDone()) {
            tasks.remove(0);
        }
    }

    public static void setPos(Vec3d pos) {
        to = pos;
        if (closeBy(currentPosition, to)) return;

        // Example of adding tasks:
        tasks.add(new WaitTask(3));
        tasks.add(new TeleportTo(pos, true));

        tasks.add(new SendPackets(20, true));
        // Continue with the rest of your pathfinder code...
    }

}
