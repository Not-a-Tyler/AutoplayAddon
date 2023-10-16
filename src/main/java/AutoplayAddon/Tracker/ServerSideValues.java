package AutoplayAddon.Tracker;
import AutoplayAddon.AutoPlay.Movement.GotoUtil;
import AutoplayAddon.AutoPlay.Movement.Movement;
import meteordevelopment.meteorclient.events.game.GameLeftEvent;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.orbit.EventPriority;
import net.minecraft.block.BlockState;
import net.minecraft.network.packet.c2s.handshake.HandshakeC2SPacket;
import net.minecraft.network.packet.c2s.login.LoginHelloC2SPacket;
import net.minecraft.network.packet.c2s.play.*;
import net.minecraft.network.packet.c2s.query.*;
import net.minecraft.network.packet.s2c.play.*;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import static meteordevelopment.meteorclient.MeteorClient.mc;

public class ServerSideValues {

    private static final int INITIAL_SIZEp = 8;

    private static long[] timesp = new long[INITIAL_SIZEp];
    private static long[] countsp = new long[INITIAL_SIZEp];
    private static final long intervalp = (long) (7.0 * 1.0e9);
    private static long lastHandshakeTime;
    private static long minTimep;
    private static long sump;
    private static int headp; // inclusive
    public static int ticks;
    private static int tailp; // exclusive
    public static boolean hasMoved, clientIsFloating = false;
    public static int lastSyncId;
    static double prevy, prevx, prevz;
    static long lastLimitedPacket = -1;
    public static Vec3d tickpos = new Vec3d(0,0,0);

    public static int i, i2, aboveGroundTickCount, limitedPackets;
    private static int receivedMovePacketCount, knownMovePacketCount, knownMovePacketCount2, receivedMovePacketCount2, lasttick, allowedPlayerTicksPredict, allowedPlayerTicks;


    public static int delta() {
        return predictallowedPlayerTicks() - (i2 + i);
    }
    public static int predictallowedPlayerTicks() {
        allowedPlayerTicksPredict = allowedPlayerTicks;
        allowedPlayerTicksPredict += (System.currentTimeMillis() / 50) - lasttick;
        return Math.max(allowedPlayerTicksPredict, 1);
    }



    @EventHandler(priority = EventPriority.LOWEST)
    private static void onTick(TickEvent.Pre event) {
        ++ticks;
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
        knownMovePacketCount2 = receivedMovePacketCount2;
        i = 0;
        i2 = 0;
    }


    @EventHandler()
    private static void onLeaveServer(GameLeftEvent event) {
        lastSyncId = 0;
        lastLimitedPacket = -1;
        tickpos = new Vec3d(0,0,0);
        hasMoved = false;
        i = 0;
        i2 = 0;
        allowedPlayerTicks = 1;
    }



    @EventHandler(priority = EventPriority.LOWEST - 2)
    private static void onSendPacket(PacketEvent.Send event) {
        //System.out.println("Packet Type: " + event.packet.toString());

        if (event.packet instanceof HandshakeC2SPacket) lastHandshakeTime = System.nanoTime();
        if (event.packet instanceof LoginHelloC2SPacket) {
            sump = 0;
            headp = 0;
            tailp = 0;
            countsp = new long[INITIAL_SIZEp];
            timesp = new long[INITIAL_SIZEp];
            updateAndAdd(1, lastHandshakeTime);
        }
        if (!(event.packet instanceof QueryPingC2SPacket || event.packet instanceof QueryRequestC2SPacket || event.packet instanceof HandshakeC2SPacket)) updateAndAdd(1, System.nanoTime());

        if (event.packet instanceof PlayerMoveC2SPacket packet) {
            HandleMovepacket(packet, true);
        }
        if (event.packet instanceof PlayerInteractBlockC2SPacket packet) {
            if(!handleUse()) {
                event.setCancelled(true);
                event.cancel();
            }
        }
        if (event.packet instanceof PlayerInteractItemC2SPacket packet) {
            if (!handleUse()) {
                event.setCancelled(true);
                event.cancel();
            }
        }
    }


    public static void updateAndAdd(final long count, final long currTime) {
        //System.out.println("sum is " + sump + " rate is " + getRate());
        // Calculate the minimum valid time directly without a temporary variable.
        minTimep = currTime - intervalp;
        // Loop to check and remove outdated values from the circular buffer.
        while (headp != tailp && timesp[headp] < minTimep) {
            sump -= countsp[headp];
            countsp[headp] = 0;
            headp = (headp + 1) % timesp.length;
        }
        if (currTime - minTimep < 0) return;
        int nextTail = (tailp + 1) % timesp.length;
        if (nextTail == headp) {
            int oldLength = timesp.length;
            long[] newTimes = new long[oldLength * 2];
            long[] newCounts = new long[oldLength * 2];

            int size;
            if (tailp >= headp) {
                // If data is ordered sequentially in the old array, just copy it over.
                size = tailp - headp;
                System.arraycopy(timesp, headp, newTimes, 0, size);
                System.arraycopy(countsp, headp, newCounts, 0, size);
            } else {
                // If data wraps around in the old array, copy in two parts.
                int firstPartSize = oldLength - headp;
                size = firstPartSize + tailp;

                System.arraycopy(timesp, headp, newTimes, 0, firstPartSize);
                System.arraycopy(timesp, 0, newTimes, firstPartSize, tailp);

                System.arraycopy(countsp, headp, newCounts, 0, firstPartSize);
                System.arraycopy(countsp, 0, newCounts, firstPartSize, tailp);
            }

            timesp = newTimes;
            countsp = newCounts;
            headp = 0;
            tailp = size;
            nextTail = (tailp + 1) % timesp.length;
        }
        timesp[tailp] = currTime;
        countsp[tailp] += count;
        sump += count;
        tailp = nextTail;
    }


    public static Boolean canSendPackets(final long count, final long currTime) {
        // Duplicate necessary variables to prevent modification
        long predictedSum = sump;
        long minTime = currTime - intervalp;
        int tmpHead = headp;
        int tmpTail = tailp;
        long[] tmpTimes = timesp.clone();
        long[] tmpCounts = countsp.clone();

        // Loop to check and remove outdated values from the circular buffer.
        while (tmpHead != tmpTail && tmpTimes[tmpHead] < minTime) {
            predictedSum -= tmpCounts[tmpHead];
            tmpCounts[tmpHead] = 0;
            tmpHead = (tmpHead + 1) % tmpTimes.length;
        }

        if (currTime - minTime < 0) return true;

        int nextTail = (tmpTail + 1) % tmpTimes.length;

        if (nextTail == tmpHead) {
            int oldLength = tmpTimes.length;
            long[] newTimes = new long[oldLength * 2];
            long[] newCounts = new long[oldLength * 2];

            int size;
            if (tmpTail >= tmpHead) {
                size = tmpTail - tmpHead;
                System.arraycopy(tmpTimes, tmpHead, newTimes, 0, size);
                System.arraycopy(tmpCounts, tmpHead, newCounts, 0, size);
            } else {
                int firstPartSize = oldLength - tmpHead;
                size = firstPartSize + tmpTail;

                System.arraycopy(tmpTimes, tmpHead, newTimes, 0, firstPartSize);
                System.arraycopy(tmpTimes, 0, newTimes, firstPartSize, tmpTail);

                System.arraycopy(tmpCounts, tmpHead, newCounts, 0, firstPartSize);
                System.arraycopy(tmpCounts, 0, newCounts, firstPartSize, tmpTail);
            }

            tmpCounts = newCounts;
            tmpTail = size;
        }

        tmpCounts[tmpTail] += count;
        predictedSum += count;
        double rate = (double)predictedSum / ((double)intervalp * 1.0E-9);
        return rate < 490;
    }


    @EventHandler(priority = EventPriority.LOWEST - 2)
    private static void onRecievePacket(PacketEvent.Send event) {
        if (event.packet instanceof OpenScreenS2CPacket packet) {
            lastSyncId = packet.getSyncId();
        }
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
            ChatUtils.error("Packet spam detected, server reset i value");
            i = 0;
            i2 = 1;
        }
        if (hasRot || d10 > 0) {
            allowedPlayerTicks -= 1;
        } else {
            allowedPlayerTicks = 20;
        }


        String packetType = "unknown";
        if (packet instanceof PlayerMoveC2SPacket.Full) packetType = "Full";
        if (packet instanceof PlayerMoveC2SPacket.OnGroundOnly) packetType = "OnGroundOnly";
        if (packet instanceof PlayerMoveC2SPacket.PositionAndOnGround) packetType = "PositionAndOnGround";
        if (packet instanceof PlayerMoveC2SPacket.LookAndOnGround) packetType = "LookAndOnGround";
        if (d10 > 0) {
            System.out.println(packetType + " allowed: " + allowedPlayerTicks + " i: " + (i2 + i) + " delta: " + delta() + " MOVED D10: " + d10 + " to " + (int) d0 + " " + (int) d1 + " " + (int) d2);
        } else {
            System.out.println(packetType + " allowed: " + allowedPlayerTicks + " i: " + (i2 + i) + " delta: " + delta());
        }


        if (hasPos) {
            prevx = packet.getX(0);
            prevy = packet.getY(0);
            prevz = packet.getZ(0);
        }
        clientIsFloating = d7 >= -0.03125D && noBlocksAround();
    }


    public static int threshhold = 305;
    public static Boolean handleUse() {
        if (lastLimitedPacket != -1 && System.currentTimeMillis() - lastLimitedPacket < threshhold && limitedPackets++ >= 8) {
            return false;
        }
        if (lastLimitedPacket == -1 || System.currentTimeMillis() - lastLimitedPacket >= threshhold) { // Paper
            lastLimitedPacket = System.currentTimeMillis();
            limitedPackets = 0;
            return true;
        }
        return true;
    }


    public static boolean canPlace() {
        if (lastLimitedPacket != -1 && System.currentTimeMillis() - lastLimitedPacket < threshhold && (limitedPackets + 1) >= 8) {
            return false;
        }
        return true;
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
