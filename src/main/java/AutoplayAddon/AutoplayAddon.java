package AutoplayAddon;
import AutoplayAddon.Tracker.BlockCache;
import AutoplayAddon.Tracker.ServerSideValues;
import meteordevelopment.meteorclient.MeteorClient;
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
    public static ServerSideValues values = new ServerSideValues();


    public static BlockCache blockCache = new BlockCache();

    @Override
    public void onInitialize() {
        values.init();
        LOG.info("Initializing AutoplayAddon");

        Modules.get().add(new Disabler());
        Modules.get().add(new Speedrun());
        Modules.get().add(new TeleportInfo());
        Modules.get().add(new BlockDebug());
        Modules.get().add(new ClickTp());
        Modules.get().add(new InfiniteAura());
        Modules.get().add(new Follower());
        Modules.get().add(new StacisBotTest());
        Modules.get().add(new LOTEST());
        Modules.get().add(new SimpleClickTp());
        Modules.get().add(new LongDistanceTest());
        Modules.get().add(new SpinBot());

        Commands.add(new Stop());
        Commands.add(new TpTo());
        Commands.add(new TestCommand());
        Commands.add(new TestCommand2());
        Commands.add(new Mine());
        Commands.add(new TP2cam());
        Commands.add(new Teleport());
        Commands.add(new Craft());
        Commands.add(new ItemCollect());
        Commands.add(new SearchFor());
        Commands.add(new findcollectableblock());
        // You can call this method to find the nearest block to a given position


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
            .getModContainer("autoplay-addon")
            .get().getMetadata()
            .getCustomValue("github:sha")
            .getAsString();
        return commit.isEmpty() ? null : commit.trim();
    }

    public String getPackage() {
        return "AutoplayAddon";
    }
}
