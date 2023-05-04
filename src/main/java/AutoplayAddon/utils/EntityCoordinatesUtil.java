package AutoplayAddon.utils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;
import java.util.Optional;
import java.util.UUID;

public class EntityCoordinatesUtil {

    public static Vec3d getEntityCoordinatesByUUID(Optional<UUID> entityUUID) {
        MinecraftClient client = MinecraftClient.getInstance();

        if (client == null || client.world == null) {
            return null;
        }

        for (Entity entity : client.world.getEntities()) {
            if (entity.getUuid().equals(entityUUID)) {
                return entity.getPos();
            }
        }

        return null;
    }
}
