package AutoplayAddon.modules;

import meteordevelopment.meteorclient.events.game.GameLeftEvent;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import AutoplayAddon.utils.*;
import java.util.Arrays;
import java.util.List;
import AutoplayAddon.AutoplayAddon;






public class Speedrun extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    public Setting<Integer> amount = sgGeneral.add(new IntSetting.Builder()
        .name("Amount of logs to mine")
        .description("test")
        .defaultValue(3)
        .min(0)
        .sliderMax(10)
        .build()
    );

    public Setting<Integer> amount2 = sgGeneral.add(new IntSetting.Builder()
        .name("Amount of logs to mine")
        .description("test")
        .defaultValue(3)
        .min(0)
        .sliderMax(10)
        .build()
    );
    private final SmartMine smartMine;
    private final ItemCollection itemCollection;

    public Speedrun() {
        super(AutoplayAddon.autoplay, "speedrun", "beats minecraft");
        smartMine = new SmartMine();
        itemCollection = new ItemCollection();
    }

    @Override
    public void onActivate() {
        mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.OnGroundOnly(true));
        Thread waitForTickEventThread = new Thread(() -> {
            List<Item> targetBlocks = Arrays.asList(Items.OAK_LOG);
            for (int i = 0; i < amount.get(); i++) {
                smartMine.processBlocks(targetBlocks);
            }
            wait1sec();
            itemCollection.collect(targetBlocks);
            wait1sec();
            CraftUtil.craftItem(Items.OAK_PLANKS, 3);
            wait1sec();
            CraftUtil.craftItem(Items.STICK, 1);
            wait1sec();
            CraftUtil.craftItem(Items.CRAFTING_TABLE, 1);
            wait1sec();
            PlaceUtil.randomplace(Blocks.CRAFTING_TABLE);
            wait1sec();
            CraftUtil.craftItem(Items.WOODEN_PICKAXE, 1);
            wait1sec();
            List<Item> targetBlocks2 = Arrays.asList(Items.STONE);
            for (int i = 0; i < amount2.get(); i++) {
                smartMine.processBlocks(targetBlocks2);
            }
            wait1sec();
            List<Item> targetBlocks3 = Arrays.asList(Items.COBBLESTONE);
            itemCollection.collect(targetBlocks3);
            wait1sec();
            CraftUtil.craftItem(Items.STONE_PICKAXE, 1);
            wait1sec();
            List<Item> targetBlocks4 = Arrays.asList(Items.IRON_ORE);
            for (int i = 0; i < amount2.get(); i++) {
                smartMine.processBlocks(targetBlocks4);
            }
            wait1sec();
            List<Item> targetBlocks5 = Arrays.asList(Items.RAW_IRON);
            itemCollection.collect(targetBlocks5);
            wait1sec();
            for (int i = 0; i < 9; i++) {
                smartMine.processBlocks(targetBlocks2);
            }
            wait1sec();
            itemCollection.collect(targetBlocks3);
            wait1sec();
            List<Item> targetBlocks6 = Arrays.asList(Items.COAL_ORE);
            List<Item> targetBlocks7 = Arrays.asList(Items.COAL);
            smartMine.processBlocks(targetBlocks6);
            wait1sec();
            itemCollection.collect(targetBlocks7);
            wait1sec();
            CraftUtil.craftItem(Items.FURNACE, 1);
            wait1sec();
            PlaceUtil.randomplace(Blocks.FURNACE);
            toggle();
        });
        waitForTickEventThread.start();
    }

    @Override
    public void onDeactivate() {
        smartMine.stop();
    }

    @EventHandler
    private void onGameLeft(GameLeftEvent event){ toggle();
    }

    private void wait1sec() {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException ee) {
            ee.printStackTrace();
        }
    }



}
