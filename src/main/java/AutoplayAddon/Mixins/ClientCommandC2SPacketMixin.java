package AutoplayAddon.Mixins;

import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ClientCommandC2SPacket.class)
public interface ClientCommandC2SPacketMixin {
    @Accessor("mountJumpHeight")
    int getMountJumpHeight();

    @Mutable
    @Accessor("mountJumpHeight")
    void setMountJumpHeight(int mountJumpHeight);
    @Accessor("mode")
    ClientCommandC2SPacket.Mode getMode();

    @Mutable
    @Accessor("mode")
    void setMode(ClientCommandC2SPacket.Mode mode);
}
