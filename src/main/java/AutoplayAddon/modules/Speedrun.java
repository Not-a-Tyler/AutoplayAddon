package AutoplayAddon.modules;

import AutoplayAddon.AutoPlay.Actions.CraftUtil;
import AutoplayAddon.AutoPlay.Actions.ItemCollection;
import AutoplayAddon.AutoPlay.Actions.PlaceUtil;
import AutoplayAddon.AutoPlay.Controller.SmartMine;
import AutoplayAddon.AutoPlay.Inventory.LogToPlankTest;
import AutoplayAddon.AutoPlay.Other.WaitUtil;
import meteordevelopment.meteorclient.events.game.GameLeftEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import AutoplayAddon.AutoPlay.Inventory.Lists;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import AutoplayAddon.AutoplayAddon;






public class Speedrun extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgRender = settings.createGroup("Render");

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




    public Speedrun() {
        super(AutoplayAddon.autoplay, "speedrun", "beats minecraft");
    }

    @Override
    public void onActivate() {
        mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.OnGroundOnly(true));
        Thread waitForTickEventThread = new Thread(() -> {
            SmartMine.mineBlocks(Arrays.asList(Lists.LOG), amount.get());
            WaitUtil.wait1sec();
            ItemCollection.collect(Arrays.asList(Lists.LOG));
            WaitUtil.wait1sec();
            LogToPlankTest.Log2Plank();
            WaitUtil.wait1sec();
            CraftUtil.craftItem(Items.STICK, 1);
            WaitUtil.wait1sec();
            CraftUtil.craftItem(Items.CRAFTING_TABLE, 1);
            WaitUtil.wait1sec();
            PlaceUtil.randomplace(Blocks.CRAFTING_TABLE);
            WaitUtil.wait1sec();
            CraftUtil.craftItem(Items.WOODEN_PICKAXE, 1);
            WaitUtil.wait1sec();
            List<Item> targetBlocks2 = Collections.singletonList(Items.STONE);
            SmartMine.mineBlocks(targetBlocks2, amount2.get());
            WaitUtil.wait1sec();
            List<Item> targetBlocks3 = Collections.singletonList(Items.COBBLESTONE);
            ItemCollection.collect(targetBlocks3);
            WaitUtil.wait1sec();
            CraftUtil.craftItem(Items.STONE_PICKAXE, 1);
            WaitUtil.wait1sec();
            List<Item> targetBlocks4 = Collections.singletonList(Items.IRON_ORE);
//                SmartMine.mineBlocks(targetBlocks4);
//            WaitUtil.wait1sec();
//            List<Item> targetBlocks5 = Collections.singletonList(Items.RAW_IRON);
//            ItemCollection.collect(targetBlocks5);
//            WaitUtil.wait1sec();
//            for (int i = 0; i < 9; i++) {
//                SmartMine.mineBlocks(targetBlocks2);
//            }
            WaitUtil.wait1sec();
            ItemCollection.collect(targetBlocks3);
            WaitUtil.wait1sec();
            List<Item> targetBlocks6 = Collections.singletonList(Items.COAL_ORE);
            List<Item> targetBlocks7 = Collections.singletonList(Items.COAL);
           // SmartMine.mineBlocks(targetBlocks6);
            WaitUtil.wait1sec();
            ItemCollection.collect(targetBlocks7);
            WaitUtil.wait1sec();
            CraftUtil.craftItem(Items.FURNACE, 1);
            WaitUtil.wait1sec();
            PlaceUtil.randomplace(Blocks.FURNACE);
            toggle();
        });
        waitForTickEventThread.start();
    }

    @Override
    public void onDeactivate() {
        info("id10t");
    }

    @EventHandler
    private void onGameLeft(GameLeftEvent event){
        toggle();
    }





}
