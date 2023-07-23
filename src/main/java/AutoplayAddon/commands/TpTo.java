package AutoplayAddon.commands;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import meteordevelopment.meteorclient.commands.arguments.PlayerArgumentType;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import net.minecraft.command.CommandSource;
import net.minecraft.entity.player.PlayerEntity;
import meteordevelopment.meteorclient.commands.Command;
import static com.mojang.brigadier.Command.SINGLE_SUCCESS;
import AutoplayAddon.AutoPlay.Movement.GotoUtil;


public class TpTo extends Command {
    public TpTo() {
        super("tpto", "ban");
    }
    @Override
    public void build(LiteralArgumentBuilder<CommandSource> builder) {
        builder.then(argument("player", PlayerArgumentType.create()).executes(context -> {
            PlayerEntity e = PlayerArgumentType.get(context);
            ChatUtils.info("item" + e.getMainHandStack().getNbt().toString());
            new Thread(() -> {
                new GotoUtil().moveto(e.getX(), e.getY(), e.getZ());
            }).start();
            return SINGLE_SUCCESS;
        }));
    }
}
