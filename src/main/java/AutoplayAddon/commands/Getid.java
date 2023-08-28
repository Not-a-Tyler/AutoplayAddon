package AutoplayAddon.commands;
import AutoplayAddon.AutoPlay.Movement.Movement;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import meteordevelopment.meteorclient.commands.arguments.PlayerArgumentType;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import net.minecraft.command.CommandSource;
import net.minecraft.entity.player.PlayerEntity;
import meteordevelopment.meteorclient.commands.Command;
import static com.mojang.brigadier.Command.SINGLE_SUCCESS;


public class Getid extends Command {
    public Getid() {
        super("getid", "ban");
    }
    @Override
    public void build(LiteralArgumentBuilder<CommandSource> builder) {
        builder.then(argument("player", PlayerArgumentType.create()).executes(context -> {
            PlayerEntity e = PlayerArgumentType.get(context);
            ChatUtils.info("player id is " + e.getId());
            return SINGLE_SUCCESS;
        }));
    }
}
