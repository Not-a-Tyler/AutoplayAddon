package AutoplayAddon;
import AutoplayAddon.Tracker.BlockCache;
import AutoplayAddon.Tracker.ServerSideValues;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.addons.GithubRepo;
import meteordevelopment.meteorclient.addons.MeteorAddon;
import meteordevelopment.meteorclient.commands.Commands;
import meteordevelopment.meteorclient.systems.modules.Category;
import meteordevelopment.meteorclient.systems.modules.Modules;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientChunkEvents;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import AutoplayAddon.commands.*;
import AutoplayAddon.modules.*;
import net.minecraft.item.*;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class AutoplayAddon extends MeteorAddon {
    public static final Logger LOG = LoggerFactory.getLogger("AutoplayAddon starting");
    public static final Category autoplay = new Category("Autoplay", Items.TNT.getDefaultStack());
    public static BlockCache blockCache = new BlockCache();
    public static ExecutorService executorService = Executors.newSingleThreadExecutor();

    @Override
    public void onInitialize() {
        MeteorClient.EVENT_BUS.subscribe(ServerSideValues.class);
        MeteorClient.EVENT_BUS.subscribe(blockCache);
        LOG.info("Initializing AutoplayAddon");

        ClientChunkEvents.CHUNK_LOAD.register((world, chunk) -> {
            blockCache.addChunk(chunk);
        });


        ClientChunkEvents.CHUNK_UNLOAD.register((world, chunk) -> {
            blockCache.removeChunk(chunk);
        });
        Modules.get().add(new CobbleNuker());;
        Modules.get().add(new Door());
        Modules.get().add(new Fightbot());
        Modules.get().add(new DeleteAllTest());
        Modules.get().add(new Disabler());
        Modules.get().add(new BlockFarmer());
        Modules.get().add(new Speedrun());
        Modules.get().add(new ExplodeCalc());
        //Modules.get().add(new TeleportInfo());
        Modules.get().add(new ClickTp());
        Modules.get().add(new InfiniteAura());
        Modules.get().add(new Follower());
        Modules.get().add(new MultiSpinbot());
        Modules.get().add(new AutoSteal());
        Modules.get().add(new FreecamFly());
        //Modules.get().add(new StacisBotTest());
        Modules.get().add(new LOTEST());
        Modules.get().add(new UpFly());
        Modules.get().add(new SimpleClickTp());
        Modules.get().add(new TorchSpam());
        Modules.get().add(new BetterMine());
        Modules.get().add(new PacketLogger());
        Modules.get().add(new BackAndForth());
        Modules.get().add(new LongDistanceTest());
        Modules.get().add(new CollisionRender());
        Modules.get().add(new uhhhh());
        //Modules.get().add(new BetterFly());

        Commands.add(new Craw());
        Commands.add(new Blank());
        Commands.add(new infAnchor());
        Commands.add(new StopSleeping());
        Commands.add(new GetClosestVehicleId());
        Commands.add(new Interact());
        Commands.add(new TPBack());
        Commands.add(new SendFull());
        Commands.add(new AcceptTp());
        Commands.add(new Getid());
        Commands.add(new Smack());
        Commands.add(new AIDSon());
        Commands.add(new AIDSoff());
        Commands.add(new Trap());
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
