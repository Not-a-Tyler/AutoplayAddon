package AutoplayAddon.modules;
import AutoplayAddon.AutoplayAddon;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.Entity;
import net.minecraft.network.packet.s2c.play.*;
import net.minecraft.util.math.Vec3d;
import java.util.HashMap;
import java.util.Map;
import meteordevelopment.meteorclient.renderer.ShapeMode;


public class LOTEST extends Module {
    private final Map<Integer, Vec3d> entityPositions = new HashMap<>();
    public LOTEST() {
        super(AutoplayAddon.autoplay, "test-module-1", "bypass live overflows movement checks");
    }

    @EventHandler()
    private void onReceivePacket(PacketEvent.Receive event) {
        int EntityId = 0;
        Vec3d newPos = Vec3d.ZERO;
        if (event.packet instanceof PlayerSpawnS2CPacket) {
            PlayerSpawnS2CPacket packet = (PlayerSpawnS2CPacket) event.packet;
            EntityId = packet.getId();
            newPos = new Vec3d(packet.getX(), packet.getY(), packet.getZ());
        }
        if (event.packet instanceof EntitySpawnS2CPacket) {
            EntitySpawnS2CPacket packet = (EntitySpawnS2CPacket) event.packet;
            EntityId = packet.getId();
            newPos = new Vec3d(packet.getX(), packet.getY(), packet.getZ());
        }
        if (event.packet instanceof EntityVelocityUpdateS2CPacket) {
            EntityVelocityUpdateS2CPacket packet = (EntityVelocityUpdateS2CPacket) event.packet;
            EntityId = packet.getId();
            Vec3d currPos = entityPositions.getOrDefault(packet.getId(), Vec3d.ZERO);

            double velocityX = packet.getVelocityX() / 8000.0;
            double velocityY = packet.getVelocityY() / 8000.0;
            double velocityZ = packet.getVelocityZ() / 8000.0;

            // Just for debugging
            ChatUtils.info("Velocity for " + EntityId + " is (" + velocityX + ", " + velocityY + ", " + velocityZ + ")");

            newPos = currPos.add(velocityX, velocityY, velocityZ);
        }


        if (event.packet instanceof EntityS2CPacket) {
            EntityS2CPacket packet = (EntityS2CPacket) event.packet;

            Entity entity = packet.getEntity(mc.world);

            if (entity != null) {
                EntityId = entity.getId();
                Vec3d currPos = entityPositions.getOrDefault(EntityId, Vec3d.ZERO);
                newPos = currPos.add(packet.getDeltaX(), packet.getDeltaY(), packet.getDeltaZ());
            } else {
                ChatUtils.info("Entity is null");
            }
        }

        if (event.packet instanceof EntityPositionS2CPacket) {
            EntityPositionS2CPacket packet = (EntityPositionS2CPacket) event.packet;
            EntityId = packet.getId();
            newPos = new Vec3d(packet.getX(), packet.getY(), packet.getZ());
        }
        if (newPos != Vec3d.ZERO && EntityId != 0) {
            entityPositions.put(EntityId, newPos);
            String packetType = event.packet.getClass().getSimpleName();
            ChatUtils.info("New Position for " + EntityId + " is " + entityPositions.get(EntityId) + " from packet " + packetType);
        }
    }


    @EventHandler()
    private void onRender(Render3DEvent event) {
        for (Vec3d position : entityPositions.values()) {
            double x1 = position.x - 0.5;
            double y1 = position.y;
            double z1 = position.z - 0.5;
            double x2 = position.x + 0.5;
            double y2 = position.y + 2;  // Assuming a height of 2 blocks for simplification
            double z2 = position.z + 0.5;
            event.renderer.box(x1, y1, z1, x2, y2, z2, Color.ORANGE, Color.ORANGE, ShapeMode.Both, 0);
        }
    }



}
