package AutoplayAddon.modules;

import AutoplayAddon.AutoPlay.Movement.AIDS;
import AutoplayAddon.AutoPlay.Movement.GotoUtil;
import AutoplayAddon.AutoPlay.Other.PlayerCopyEntity;
import AutoplayAddon.AutoplayAddon;
import meteordevelopment.meteorclient.events.entity.player.PlayerMoveEvent;
import meteordevelopment.meteorclient.events.game.OpenScreenEvent;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.mixininterface.IPlayerMoveC2SPacket;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.orbit.EventPriority;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.passive.AbstractHorseEntity;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket.Mode;

public class BetterFly extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    public BetterFly() {
        super(AutoplayAddon.autoplay, "better-fly", "A better version of the creative fly");
    }

    private final Setting<Double> speed = sgGeneral.add(new DoubleSetting.Builder()
        .name("speed")
        .description("The speed to fly at.")
        .defaultValue(10)
        .min(0.1)
        .sliderMax(50)
        .build()
    );



    private PlayerCopyEntity dummy;
    private double[] playerPos;
    private float[] playerRot;
    private Entity riding;

    private boolean prevFlying;
    private float prevFlySpeed;

    @Override
    public void onActivate() {
        mc.player.noClip = true;
        mc.chunkCullingEnabled = false;

        playerPos = new double[] { mc.player.getX(), mc.player.getY(), mc.player.getZ() };
        playerRot = new float[] { mc.player.getYaw(), mc.player.getPitch() };


        if (mc.player.getVehicle() != null) {
            riding = mc.player.getVehicle();
            mc.player.getVehicle().removeAllPassengers();
        }

        if (mc.player.isSprinting()) {
            mc.player.networkHandler.sendPacket(new ClientCommandC2SPacket(mc.player, Mode.STOP_SPRINTING));
        }

        prevFlying = mc.player.getAbilities().flying;
        prevFlySpeed = mc.player.getAbilities().getFlySpeed();
    }


    @EventHandler
    private void onmOVE(PlayerMoveEvent event) {
        mc.player.noClip = true;
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        mc.player.setOnGround(false);
        mc.player.getAbilities().setFlySpeed((float) (speed.get() / 5));
        mc.player.getAbilities().flying = true;
        mc.player.setPose(EntityPose.STANDING);
    }

    @Override
    public void onDeactivate() {
        mc.player.noClip = false;
        mc.player.getAbilities().flying = prevFlying;
        mc.player.getAbilities().setFlySpeed(prevFlySpeed);

        mc.player.refreshPositionAndAngles(playerPos[0], playerPos[1], playerPos[2], playerRot[0], playerRot[1]);
        mc.player.setVelocity(Vec3d.ZERO);

        if (riding != null && mc.world.getEntityById(riding.getId()) != null) {
            mc.player.startRiding(riding);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    private void onSendPacket(PacketEvent.Send event) {
        if (event.packet instanceof ClientCommandC2SPacket) {
            event.cancel();
        }
        if (event.packet instanceof PlayerMoveC2SPacket) {
            PlayerMoveC2SPacket packet = (PlayerMoveC2SPacket) event.packet;
            if (((IPlayerMoveC2SPacket) event.packet).getTag() != 13377) {
                event.setCancelled(true);
                event.cancel();
                Vec3d packetPos = new Vec3d(packet.getX(mc.player.getX()), packet.getY(mc.player.getY()), packet.getZ(mc.player.getZ()));
                Box box = new Box(
                    packetPos.x - mc.player.getWidth() / 2,
                    packetPos.y,
                    packetPos.z - mc.player.getWidth() / 2,
                    packetPos.x + mc.player.getWidth() / 2,
                    packetPos.y + mc.player.getHeight(),
                    packetPos.z + mc.player.getWidth() / 2
                );

                if (mc.world.isSpaceEmpty(box)) {
                        //ChatUtils.info("Sending packet");
                    AIDS.setPos(packetPos);
                    ((IPlayerMoveC2SPacket) event.packet).setTag(13377);
                }
            }
        }
    }
    @EventHandler
    private void onOpenScreen(OpenScreenEvent event) {
        if (riding instanceof AbstractHorseEntity) {
            if (event.screen instanceof InventoryScreen) {
                mc.player.networkHandler.sendPacket(new ClientCommandC2SPacket(mc.player, ClientCommandC2SPacket.Mode.OPEN_INVENTORY));
                event.setCancelled(true);
            }
        }
    }

}
