package AutoplayAddon.modules;

import AutoplayAddon.AutoplayAddon;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.settings.StringSetting;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.orbit.EventPriority;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.s2c.play.EntityPositionS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket;
import net.minecraft.util.math.Vec3d;

public class LOTEST extends Module {
    private long lastUpdateTick = 0;
    private double vx = 0;
    private double vy = 0;
    private double vz = 0;
    private int targetPlayerId = -1; // Invalid ID to start with
    private final double VELOCITY_SCALE = 1/8000.0;
    private Vec3d trackPos = null;

    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    public LOTEST() {
        super(AutoplayAddon.autoplay, "test-module", "bypass live overflows movement checks");
    }
    private final Setting<String> stringtype = sgGeneral.add(new StringSetting.Builder()
        .name("player-name")
        .description("Player to follow.")
        .defaultValue("")
        .build()
    );


    private int getTargetId() {
        if (targetPlayerId != -1) return targetPlayerId; // if we've already found our player, return cached ID

        for (Entity entity : mc.world.getEntities()) {
            if (entity instanceof PlayerEntity && stringtype.get().equals(((PlayerEntity) entity).getGameProfile().getName())) {
                targetPlayerId = entity.getId();
                return targetPlayerId;
            }
        }

        return -1;
    }


    @EventHandler(priority = EventPriority.LOWEST - 1)
    private void onReceiveVelocityPacket(PacketEvent.Receive event) {
        if (!(event.packet instanceof EntityVelocityUpdateS2CPacket packet)) return;

        if (trackPos == null || packet.getId() != getTargetId()) return;

        vx = packet.getVelocityX() * VELOCITY_SCALE;
        vy = packet.getVelocityY() * VELOCITY_SCALE;
        vz = packet.getVelocityZ() * VELOCITY_SCALE;

        trackPos = trackPos.add(vx, vy, vz); // adjust position using the velocity
        lastUpdateTick = mc.world.getTime();
    }

    @EventHandler(priority = EventPriority.LOWEST - 1)
    private void onReceivePacket(PacketEvent.Receive event) {
        if (!(event.packet instanceof EntityPositionS2CPacket packet)) return;

        if (packet.getId() == getTargetId()) {
            trackPos = new Vec3d(packet.getX(), packet.getY(), packet.getZ());
            lastUpdateTick = mc.world.getTime();
        }
    }
    @EventHandler
    private void onTick(TickEvent event) {
        if (trackPos == null) return;

        // Apply velocity
        trackPos = trackPos.add(vx, vy, vz);

        // Check for outdated velocity
        if (mc.world.getTime() - lastUpdateTick > 20) {
            vx = 0;
            vy = 0;
            vz = 0;
        }
    }


    @EventHandler
    private void onRender(Render3DEvent event) {
        if (trackPos == null) return;
        double x1 = trackPos.getX();
        double y1 = trackPos.getY();
        double z1 = trackPos.getZ();
        double x2 = x1 + 1;
        double y2 = y1 + 1;
        double z2 = z1 + 1;
        event.renderer.box(x1, y1, z1, x2, y2, z2, Color.CYAN, Color.BLACK, ShapeMode.Both, 0);
    }


}
