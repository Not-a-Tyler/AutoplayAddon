package AutoplayAddon;
import meteordevelopment.meteorclient.addons.GithubRepo;
import meteordevelopment.meteorclient.addons.MeteorAddon;
import meteordevelopment.meteorclient.commands.Commands;
import meteordevelopment.meteorclient.systems.modules.Category;
import meteordevelopment.meteorclient.systems.modules.Modules;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import AutoplayAddon.commands.*;
import AutoplayAddon.modules.*;
import net.minecraft.item.*;


public class AutoplayAddon extends MeteorAddon {
    public static final Logger LOG = LoggerFactory.getLogger("AutoplayAddon starting");
    public static final Category autoplay = new Category("Autoplay", Items.TNT.getDefaultStack());




    @Override
    public void onInitialize() {
        LOG.info("Initializing AutoplayAddon");



        //misc

        Modules.get().add(new Disabler());
        Modules.get().add(new Speedrun());
        Modules.get().add(new TeleportInfo());



        Commands.add(new TP2cam());
        Commands.add(new Teleport());
        Commands.add(new testcommand());
        Commands.add(new testcommand2());
        Commands.add(new testcommand3());
        Commands.add(new testcommand4());
        Commands.add(new testcommand5());
        Commands.add(new testcommand6());
        Commands.add(new testcommand7());
        Commands.add(new testcommand8());
        Commands.add(new testcommand9());
        Commands.add(new testcommand10());
        Commands.add(new testcommand11());
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
