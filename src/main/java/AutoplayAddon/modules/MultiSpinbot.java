package AutoplayAddon.modules;

import AutoplayAddon.AutoPlay.Movement.AIDS;
import AutoplayAddon.AutoPlay.Movement.Movement;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.util.math.Vec3d;
import AutoplayAddon.AutoplayAddon;
import java.io.File;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.HashMap;
import java.util.Map;

public class MultiSpinbot extends Module {
    private static final int DOUBLE_SIZE = 8;
    private static final int FLOAT_SIZE = 4;
    private static final int BOOLEAN_SIZE = 1;
    private static final int CLIENT_DATA_SIZE = 3 * DOUBLE_SIZE + 2 * FLOAT_SIZE + BOOLEAN_SIZE;
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    public MultiSpinbot() {
        super(AutoplayAddon.autoplay, "bot-controller", "Allows multiple players to circle around a player in a synchronized fashion");
    }

    private final Setting<Type> type = sgGeneral.add(new EnumSetting.Builder<Type>()
        .name("mode")
        .description("The mode, either client or server.")
        .defaultValue(Type.Client)
        .build());

    private final Setting<Boolean> mimicsneak = sgGeneral.add(new BoolSetting.Builder()
        .name("mimic-sneak")
        .description("Clients will copy the servers sneaking status")
        .defaultValue(true)
        .build()
    );


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


    @Override
    public void onActivate() {
        init();
    }

    @Override
    public void onDeactivate() {
        Movement.rotationControl = true;
        AIDS.disable();
    }

    public void init() {

        try {
            initializeMemoryMappedFile(1024);
            for (int id = 1; id <= totalClients.get(); id++) {
                setClientId(id, (2 * Math.PI * (id-1)) / totalClients.get());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public enum Type {
        Client,
        Server
    }

    private MappedByteBuffer map;
    private final Map<Integer, Double> clientAngles = new HashMap<>();

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (type.get() == Type.Server) {
            double incrementAngle = (2 * Math.PI * spinSpeed.get() / 360.0);  // Convert the spin speed to radians per tick
            for (int id = 1; id <= totalClients.get(); id++) {
                double currentAngle = clientAngles.getOrDefault(id, 0.0);
                Vec3d pos = calculatePositionBasedOnAngle(currentAngle);
                clientAngles.put(id, currentAngle + incrementAngle);
                boolean sneakingStatus;
                float pitch, yaw;
                if (mimicsneak.get()) {
                    sneakingStatus = mc.player.isSneaking();
                } else {
                    sneakingStatus = false;
                }
                pitch = mc.player.getPitch(1.0F);
                yaw = mc.player.getYaw(1.0F);
                writeToMemoryMappedFile(id, pos, sneakingStatus, pitch, yaw);
            }
        } else if (type.get() == Type.Client) {
            if (!Movement.AIDSboolean) {
                AIDS.init(true);
            }
            ClientData clientData = readFromMemoryMappedFile(clientNumber.get());
            if (clientData != null) {
                Movement.yaw = clientData.yaw;
                Movement.pitch = clientData.pitch;
                AIDS.setPos(clientData.pos);
                mc.player.setSneaking(clientData.isSneaking);
            }
        }
    }

    private void setClientId(int id, double initialAngle) {
        clientAngles.put(id, initialAngle);
    }


    public void initializeMemoryMappedFile(int size) throws Exception {
        // Get the user's home directory in a platform-independent manner.
        String userHome = System.getProperty("user.home");
        String separator = System.getProperty("file.separator");
        String filePath = userHome + separator + "test" + separator + "positions.dat";

        // Ensure the directory exists
        File directory = new File(userHome + separator + "test");
        if (!directory.exists()) {
            directory.mkdirs();
        }

        RandomAccessFile file = new RandomAccessFile(filePath, "rw");
        FileChannel channel = file.getChannel();

        // Make sure the file is at least the specified size
        if (file.length() < size) {
            file.setLength(size);
        }

        map = channel.map(FileChannel.MapMode.READ_WRITE, 0, size);
        channel.close();
        file.close();
    }


    private void writeToMemoryMappedFile(int clientId, Vec3d pos, boolean isSneaking, float pitch, float yaw) {
        map.position(clientId * CLIENT_DATA_SIZE);
        map.putDouble(pos.x);
        map.putDouble(pos.y);
        map.putDouble(pos.z);
        map.putFloat(pitch);
        map.putFloat(yaw);
        map.put((byte) (isSneaking ? 1 : 0));
    }

    private static class ClientData {
        public Vec3d pos;
        public boolean isSneaking;
        public float pitch;
        public float yaw;
    }

    public ClientData readFromMemoryMappedFile(int clientId) {
        map.position(clientId * CLIENT_DATA_SIZE);
        double x = map.getDouble();
        double y = map.getDouble();
        double z = map.getDouble();
        float pitch = map.getFloat();
        float yaw = map.getFloat();
        boolean isSneaking = map.get() == 1;
        ClientData clientData = new ClientData();
        clientData.pos = new Vec3d(x, y, z);
        clientData.isSneaking = isSneaking;
        clientData.pitch = pitch;
        clientData.yaw = yaw;
        return clientData;
    }

    private Vec3d calculatePositionBasedOnAngle(double angle) {
        double dx = Math.cos(angle) * distanceFromPlayer.get();
        double dz = Math.sin(angle) * distanceFromPlayer.get();
        return new Vec3d(mc.player.getX() + dx, mc.player.getY(), mc.player.getZ() + dz);
    }
}
