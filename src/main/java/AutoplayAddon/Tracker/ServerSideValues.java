package AutoplayAddon.Tracker;
import AutoplayAddon.AutoPlay.Movement.GotoUtil;
import AutoplayAddon.AutoPlay.Movement.Movement;
import meteordevelopment.meteorclient.events.game.GameJoinedEvent;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.orbit.EventPriority;
import net.minecraft.block.BlockState;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.network.packet.s2c.play.EntityPositionS2CPacket;
import net.minecraft.network.packet.s2c.play.GameJoinS2CPacket;
import net.minecraft.network.packet.s2c.play.OpenScreenS2CPacket;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import static meteordevelopment.meteorclient.MeteorClient.mc;

public class ServerSideValues {


    public static boolean hasMoved, clientIsFloating = false;
    public static int lastSyncId;
    static double prevx;
    static double prevy;
    static double prevz= 0;
    public static Vec3d tickpos = new Vec3d(0,0,0);
    public static int i, i2, allowedPlayerTicks, aboveGroundTickCount;
    private static int receivedMovePacketCount, knownMovePacketCount, knownMovePacketCount2, receivedMovePacketCount2, lasttick;


    public static int delta() {
        return allowedPlayerTicks - (i2 + i);
    }
    public static int ii() {
        return (i2 + i);
    }


    @EventHandler(priority = EventPriority.LOWEST)
    private static void onTick(TickEvent.Pre event) {
        if (clientIsFloating) {
            ++aboveGroundTickCount;
        } else {
            clientIsFloating = false;
            aboveGroundTickCount = 0;
        }
        hasMoved = false;
        if (mc.player == null) return;
        if (Movement.AIDSboolean || GotoUtil.currentlyMoving) {
            tickpos = Movement.currentPosition;
        } else {
            tickpos = mc.player.getPos();
        }
        knownMovePacketCount = receivedMovePacketCount;
        receivedMovePacketCount2 = knownMovePacketCount2;
        i = 0;
        i2 = 0;
    }

    public static void HandleMovePacketSafe(PlayerMoveC2SPacket packet) {
        ++receivedMovePacketCount2;
        i2 = (receivedMovePacketCount2 - knownMovePacketCount2) - (receivedMovePacketCount - knownMovePacketCount);
        HandleMovepacket(packet, false);
    }

    public static void HandleMovepacket(PlayerMoveC2SPacket packet, Boolean setI) {
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

        if (setI) {
            ++receivedMovePacketCount;
            i = receivedMovePacketCount - knownMovePacketCount;
        }
        allowedPlayerTicks += (System.currentTimeMillis() / 50) - lasttick;
        allowedPlayerTicks = Math.max(allowedPlayerTicks, 1);
        lasttick = (int) (System.currentTimeMillis() / 50);
        if ((i2 + i) > Math.max(allowedPlayerTicks, 5)) {
            i = 0;
            i2 = 1;
        }
        if (hasRot || d10 > 0) {
            allowedPlayerTicks -= 1;
        } else {
            allowedPlayerTicks = 20;
        }

//        String packetType = "unknown";
//        if (packet instanceof PlayerMoveC2SPacket.Full) packetType = "Full";
//        if (packet instanceof PlayerMoveC2SPacket.OnGroundOnly) packetType = "OnGroundOnly";
//        if (packet instanceof PlayerMoveC2SPacket.PositionAndOnGround) packetType = "PositionAndOnGround";
//        if (packet instanceof PlayerMoveC2SPacket.LookAndOnGround) packetType = "LookAndOnGround";
//        if (d10 > 0) {
//            ChatUtils.info(packetType + " allowed: " + allowedPlayerTicks + " i: " + (i2 + i) + " delta: " + delta() + " MOVED D10: " + d10);
//        } else {
//            ChatUtils.info(packetType + " allowed: " + allowedPlayerTicks + " i: " + (i2 + i) + " delta: " + delta());
//        }


        if (hasPos) {
            prevx = packet.getX(0);
            prevy = packet.getY(0);
            prevz = packet.getZ(0);
        }
        clientIsFloating = d7 >= -0.03125D && noBlocksAround();
    }

    @EventHandler()
    private static void onJoinServer(GameJoinedEvent event) {
        prevz= 0;
        tickpos = new Vec3d(0,0,0);
        hasMoved = false;
        i = 0;
        i2 = 0;
        allowedPlayerTicks = 1;
    }

    @EventHandler(priority = EventPriority.LOWEST - 2)
    private static void onSendPacket(PacketEvent.Send event) {
        if (event.packet instanceof PlayerMoveC2SPacket packet) {
            HandleMovepacket(packet, true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST - 2)
    private static void onRecievePacket(PacketEvent.Send event) {
        if (event.packet instanceof OpenScreenS2CPacket packet) {
            lastSyncId = packet.getSyncId();
        }
        if (event.packet instanceof EntityPositionS2CPacket packet) {
        }
    }


    private static boolean noBlocksAround() {
        // Paper start - stop using streams, this is already a known fixed problem in Entity#move
        Box box = mc.player.getBoundingBox().expand(0.0625D).stretch(0.0D, -0.55D, 0.0D);
        int minX = MathHelper.floor(box.minX);
        int minY = MathHelper.floor(box.minY);
        int minZ = MathHelper.floor(box.minZ);
        int maxX = MathHelper.floor(box.maxX);
        int maxY = MathHelper.floor(box.maxY);
        int maxZ = MathHelper.floor(box.maxZ);
        BlockPos.Mutable pos = new BlockPos.Mutable();
        if(mc.world == null) return false;
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
