package AutoplayAddon.AutoPlay.Other;

import AutoplayAddon.AutoplayAddon;
import AutoplayAddon.Mixins.ClientConnectionInvokerMixin;
import AutoplayAddon.Tracker.ServerSideValues;
import meteordevelopment.meteorclient.utils.player.ChatUtils;

import java.util.LinkedList;
import java.util.Queue;

import static meteordevelopment.meteorclient.MeteorClient.mc;

public class Packet {
    public static final Queue<net.minecraft.network.packet.Packet<?>> packetQueue = new LinkedList<>();
    public static void sendAllPacketsInQueue() {
        ChatUtils.info("packetqueue sending " + packetQueue.size() + " packets");
        AutoplayAddon.executorService.submit(() -> {
            int amount = packetQueue.size();
            while (!packetQueue.isEmpty()) {
                net.minecraft.network.packet.Packet<?> packet = packetQueue.poll();
                ((ClientConnectionInvokerMixin) mc.getNetworkHandler().getConnection())._sendImmediately(packet, null, true);
            }
            ServerSideValues.updateAndAdd(amount, System.nanoTime());
        });
    }
    public static void sendPacket(net.minecraft.network.packet.Packet<?> packet) {
        ServerSideValues.updateAndAdd(1, System.nanoTime());
        ((ClientConnectionInvokerMixin) mc.getNetworkHandler().getConnection())._sendImmediately(packet, null, true);
    }
}
