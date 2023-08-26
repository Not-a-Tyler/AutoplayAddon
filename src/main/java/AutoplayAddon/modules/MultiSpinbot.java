package AutoplayAddon.modules;

import AutoplayAddon.AutoPlay.Movement.AIDS;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.util.math.Vec3d;
import AutoplayAddon.AutoplayAddon;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.HashMap;
import java.util.Map;

public class MultiSpinbot extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    public MultiSpinbot() {
        super(AutoplayAddon.autoplay, "multi-spinbot", "Allows multiple players to circle around a player in a synchronized fashion");
    }

    private final Setting<Mode> mode = sgGeneral.add(new EnumSetting.Builder<Mode>()
        .name("mode")
        .description("The mode, either client or server.")
        .defaultValue(Mode.Client)
        .build());

    private final Setting<Integer> distanceFromPlayer = sgGeneral.add(new IntSetting.Builder()
        .name("distance-from-player")
        .description("Distance each client should maintain from the server player.")
        .defaultValue(20)
        .min(0)
        .sliderMax(300)
        .build());


    private final Setting<Integer> spinSpeed = sgGeneral.add(new IntSetting.Builder()
        .name("spin-speed")
        .description("Distance each client should maintain from the server player.")
        .defaultValue(20)
        .min(0)
        .sliderMax(300)
        .build());

    private final Setting<Integer> totalClients = sgGeneral.add(new IntSetting.Builder()
        .name("total-clients")
        .description("Total number of clients.")
        .defaultValue(20)
        .min(0)
        .sliderMax(300)
        .build());

    private final Setting<Integer> clientNumber = sgGeneral.add(new IntSetting.Builder()
        .name("client-number")
        .description("Unique number for each client.")
        .defaultValue(1)
        .min(1)
        .sliderMax(300)
        .build());

    public enum Mode {
        Client,
        Server
    }

    private MappedByteBuffer map;
    private final Map<Integer, Double> clientAngles = new HashMap<>();

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (mode.get() == Mode.Server) {
            double incrementAngle = (2 * Math.PI * spinSpeed.get() / 360.0);  // Convert the spin speed to radians per tick
            for (int id = 1; id <= totalClients.get(); id++) {
                double currentAngle = clientAngles.getOrDefault(id, 0.0);
                Vec3d pos = calculatePositionBasedOnAngle(currentAngle);
                try {
                    writeToMemoryMappedFile(id, pos);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                clientAngles.put(id, currentAngle + incrementAngle);
            }
        } else if (mode.get() == Mode.Client) {
            Vec3d targetPos = readFromMemoryMappedFile(clientNumber.get());
            if (targetPos != null) {
                AIDS.setPos(targetPos);
            }
        }
    }

    @Override
    public void onActivate() {
        try {
            initializeMemoryMappedFile(1024);
            // Set initial angles for clients
            for (int id = 1; id <= totalClients.get(); id++) {
                setClientId(id, (2 * Math.PI * (id-1)) / totalClients.get());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setClientId(int id, double initialAngle) {
        clientAngles.put(id, initialAngle);
    }


    public void initializeMemoryMappedFile(int size) throws Exception {
        RandomAccessFile file = new RandomAccessFile("C:\\test\\positions.dat", "rw");
        FileChannel channel = file.getChannel();
        map = channel.map(FileChannel.MapMode.READ_WRITE, 0, size);
        channel.close();
        file.close();
    }

    private void writeToMemoryMappedFile(int clientId, Vec3d pos) throws IOException {
        map.position(clientId * 24);
        map.putDouble(pos.x);
        map.putDouble(pos.y);
        map.putDouble(pos.z);
    }

    private Vec3d calculatePositionBasedOnAngle(double angle) {
        double dx = Math.cos(angle) * distanceFromPlayer.get();
        double dz = Math.sin(angle) * distanceFromPlayer.get();
        return new Vec3d(mc.player.getX() + dx, mc.player.getY(), mc.player.getZ() + dz);
    }

    public Vec3d readFromMemoryMappedFile(int clientId) {
        if (clientId < 0) {
            return null;
        }
        map.position(clientId * 24);
        double x = map.getDouble();
        double y = map.getDouble();
        double z = map.getDouble();
        return new Vec3d(x, y, z);
    }
}
