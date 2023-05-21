package AutoplayAddon;
import meteordevelopment.meteorclient.addons.GithubRepo;
import meteordevelopment.meteorclient.addons.MeteorAddon;
import meteordevelopment.meteorclient.commands.Commands;
import meteordevelopment.meteorclient.systems.modules.Category;
import meteordevelopment.meteorclient.systems.modules.Modules;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.block.Block;
import net.minecraft.util.math.BlockPos;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import AutoplayAddon.commands.*;
import AutoplayAddon.modules.*;
import net.minecraft.item.*;

import java.util.HashMap;


public class AutoplayAddon extends MeteorAddon {
    public static final Logger LOG = LoggerFactory.getLogger("AutoplayAddon starting");
    public static final Category autoplay = new Category("Autoplay", Items.TNT.getDefaultStack());




    @Override
    public void onInitialize() {

        LOG.info("Initializing AutoplayAddon");


        Modules.get().add(new Disabler());
        Modules.get().add(new Speedrun());
        Modules.get().add(new TeleportInfo());
        Modules.get().add(new BlockDebug());


        Commands.add(new Mine());
        Commands.add(new TP2cam());
        Commands.add(new Teleport());
        Commands.add(new Craft());
        Commands.add(new ItemCollect());
        Commands.add(new SearchFor());

    }

    @Override
    public void onRegisterCategories() {
        Modules.registerCategory(autoplay);
    }

    @Override
    public String getWebsite() {
        return "https://github.com/Not-a-Tyler/AutoplayAddon";
    }

    @Override
    public GithubRepo getRepo() {
        return new GithubRepo("Not-a-Tyler", "AutoplayAddon");
    }

    @Override
    public String getCommit() {
        String commit = FabricLoader
            .getInstance()
            .getModContainer("AutoplayAddon")
            .get().getMetadata()
            .getCustomValue("github:sha")
            .getAsString();
        return commit.isEmpty() ? null : commit.trim();
    }

    public String getPackage() {
        return "AutoplayAddon";
    }
}
