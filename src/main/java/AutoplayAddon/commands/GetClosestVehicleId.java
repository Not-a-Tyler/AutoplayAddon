package AutoplayAddon.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import meteordevelopment.meteorclient.commands.Command;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import net.minecraft.command.CommandSource;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;

import java.util.Arrays;
import java.util.List;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;
import static meteordevelopment.meteorclient.MeteorClient.mc;

public class GetClosestVehicleId extends Command {

    private final List<EntityType<?>> vehicleTypes = Arrays.asList(
        EntityType.BOAT,
        EntityType.MINECART
        // Add other vehicle types if needed
    );

    public GetClosestVehicleId() {
        super("getClosestVehicleId", "Gets the closest vehicle entity id.");
    }

    @Override
    public void build(LiteralArgumentBuilder<CommandSource> builder) {
        builder.executes(context -> {
            Entity closestVehicle = null;
            double closestDistance = Double.MAX_VALUE;

            for (Entity entity : mc.world.getEntities()) {
                if (vehicleTypes.contains(entity.getType())) {
                    double distance = mc.player.distanceTo(entity);
                    if (distance < closestDistance) {
                        closestDistance = distance;
                        closestVehicle = entity;
                    }
                }
            }

            if (closestVehicle != null) {
                ChatUtils.info("Closest vehicle entity id is: " + closestVehicle.getId());
            } else {
                ChatUtils.info("No vehicles found nearby.");
            }

            return SINGLE_SUCCESS;
        });
    }
}
