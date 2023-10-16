package AutoplayAddon.Mixins;

import net.minecraft.network.ClientConnection;
import net.minecraft.network.PacketCallbacks;
import net.minecraft.network.packet.Packet;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(ClientConnection.class)
public interface ClientConnectionInvokerMixin {
    @Invoker("sendImmediately")
    void _sendImmediately(Packet<?> packet, PacketCallbacks callbacks, boolean flush);
}
