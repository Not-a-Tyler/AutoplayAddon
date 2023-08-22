package AutoplayAddon.Tracker;
import AutoplayAddon.AutoplayAddon;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.orbit.EventPriority;
import net.minecraft.block.BlockState;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

import static meteordevelopment.meteorclient.MeteorClient.mc;

public class ServerSideValues {


    public static boolean hasMoved, clientIsFloating = false;
    static double prevx;
    static double prevy;
    static double prevz= 0;
    public static Vec3d tickpos = new Vec3d(0,0,0);
    public static int i, allowedPlayerTicks, aboveGroundTickCount = 0;
    private static int receivedMovePacketCount, knownMovePacketCount, lasttick = 0;


    public static int delta() {
        return allowedPlayerTicks - i;
    }


    @EventHandler(priority = EventPriority.LOWEST)
    private static void onTick(TickEvent.Pre event) {
        if (clientIsFloating) {
            if (++aboveGroundTickCount > 80) {
                //ChatUtils.info("Client is floatin");
            }
        } else {
            clientIsFloating = false;
            aboveGroundTickCount = 0;
        }
        hasMoved = false;
        if (mc.player == null) return;
        tickpos = mc.player.getPos();
        knownMovePacketCount = receivedMovePacketCount;
    }

    public static void HandleMovepacket(PlayerMoveC2SPacket packet) {
        double d10 = 0;
        double d0 = packet.getX(prevx);
        double d1 = packet.getY(prevy);
        double d2 = packet.getZ(prevz);
        boolean hasPos = packet.changesPosition();
        boolean hasRot = packet.changesLook();

        double currDeltaX = d0 - prevx;
        double currDeltaY = d1 - prevy;
        double currDeltaZ = d2 - prevz;
        if (hasPos) {
            d10 = (currDeltaX * currDeltaX + currDeltaY * currDeltaY + currDeltaZ * currDeltaZ);
        }

        double d6 = d0 - tickpos.x;
        double d7 = d1 - tickpos.y;
        double d8 = d2 - tickpos.z;

        d10 = Math.max((d6 * d6 + d7 * d7 + d8 * d8), d10);
        if (d10 > 0) {
            hasMoved = true;
        }

        ++receivedMovePacketCount;
        i = receivedMovePacketCount - knownMovePacketCount;
        allowedPlayerTicks += (System.currentTimeMillis() / 50) - lasttick;
        allowedPlayerTicks = Math.max(allowedPlayerTicks, 1);
        lasttick = (int) (System.currentTimeMillis() / 50);
        if (i > Math.max(allowedPlayerTicks, 5)) {
            i = 1;
        }

        if (hasRot || d10 > 0) {
            allowedPlayerTicks -= 1;
        } else {
            allowedPlayerTicks = 20;
        }
//            Vec3d playermoveloc = new Vec3d(currDeltaX, currDeltaY, currDeltaZ);
//            Vec3d vec3d1 = CanTeleport.adjustMovementForCollisions(mc.player.getBoundingBox(), playermoveloc);
//            ChatUtils.info(playermoveloc + " current delta Y" + currDeltaY + ", new y" + vec3d1.y);
//            Boolean verticalCollision = currDeltaY != vec3d1.y;
//            Boolean verticalCollisionBelow = verticalCollision && currDeltaY < 0.0D;


        if (d10 > 0) {
            ChatUtils.info("allowed: " + allowedPlayerTicks + " i: " + i + " delta: " + delta() + " MOVED D10: " + d10);
        } else {
            ChatUtils.info("allowed: " + allowedPlayerTicks + " i: " + i + " delta: " + delta());
        }
        if (hasPos) {
            prevx = packet.getX(0);
            prevy = packet.getY(0);
            prevz = packet.getZ(0);
        }
        clientIsFloating = d7 >= -0.03125D && noBlocksAround();
    }

    @EventHandler(priority = EventPriority.LOWEST - 2)
    private static void onSendPacket(PacketEvent.Send event) {
        if (event.packet instanceof PlayerMoveC2SPacket) {
            PlayerMoveC2SPacket packet = (PlayerMoveC2SPacket) event.packet;
            HandleMovepacket(packet);
        }
    }

    public static int floor(double value) {
        int i = (int)value;
        return value < (double)i ? i - 1 : i;
    }

    private static boolean noBlocksAround() {
        // Paper start - stop using streams, this is already a known fixed problem in Entity#move
        Box box = mc.player.getBoundingBox().expand(0.0625D).stretch(0.0D, -0.55D, 0.0D);
        int minX = floor(box.minX);
        int minY = floor(box.minY);
        int minZ = floor(box.minZ);
        int maxX = floor(box.maxX);
        int maxY = floor(box.maxY);
        int maxZ = floor(box.maxZ);
        //String sep = ", ";
        //ChatUtils.info(minX + sep +  minY + sep + minZ + sep + maxX + sep + maxY + sep + maxZ);
        BlockPos.Mutable pos = new BlockPos.Mutable();

        for (int y = minY; y <= maxY; ++y) {
            for (int z = minZ; z <= maxZ; ++z) {
                for (int x = minX; x <= maxX; ++x) {
                    pos.set(x, y, z);
                    BlockState type = mc.world.getBlockState(pos);
                    if (type != null && !type.isAir()) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

}
