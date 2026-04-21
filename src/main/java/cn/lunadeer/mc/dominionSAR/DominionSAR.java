package cn.lunadeer.mc.dominionSAR;

import cn.lunadeer.dominion.api.DominionAPI;
import cn.lunadeer.dominion.api.dtos.flag.EnvFlag;
import cn.lunadeer.dominion.api.dtos.flag.Flags;
import cn.lunadeer.mc.dominionSAR.providers.*;
import cn.lunadeer.mc.dominionSAR.utils.configuration.ConfigurationManager;
import cn.lunadeer.mc.dominionSAR.utils.scheduler.Scheduler;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static cn.lunadeer.mc.dominionSAR.EventsHandler.renderDominion;

public final class DominionSAR extends JavaPlugin {

    private static DominionSAR instance;
    private static DominionAPI dominionAPI;
    private final List<MapProvider> mapProviders = new ArrayList<>();

    public static EnvFlag SHOW_ON_WEB = new EnvFlag(
            "show_on_web",
            "Show On Web Map",
            "Whether to show this dominion on web map.",
            true,
            true,
            Material.MAP
    );

    @Override
    public void onEnable() {
        // Plugin startup logic
        instance = this;
        new Scheduler(this);
        // prepare config
        try {
            ConfigurationManager.load(Configuration.class, new File(getDataFolder(), "config.yml"));
        } catch (Exception e) {
            e.printStackTrace();
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }
        // register events
        Bukkit.getPluginManager().registerEvents(new EventsHandler(), this);
        // initialize providers
        if (Configuration.getBluemapMode() == Configuration.BlueMapMode.API) {
            mapProviders.add(new BlueMapApiMode());
        } else if (Configuration.getBluemapMode() == Configuration.BlueMapMode.STANDALONE) {
            mapProviders.add(new BlueMapStandaloneMode());
        }
        if (Configuration.mapProvider.dynmap) {
            mapProviders.add(new Dynmap());
        }
        if (Configuration.mapProvider.pl3xMap) {
            mapProviders.add(new Pl3xMap());
        }
        if (Configuration.mapProvider.squareMap) {
            mapProviders.add(new SquareMap());
        }
        // initialize dominion api
        try {
            Flags.registerEnvFlag(this, SHOW_ON_WEB);
            Flags.applyNewCustomFlags();
        } catch (Exception e) {
            e.printStackTrace();
            Bukkit.getPluginManager().disablePlugin(this);
        }

        Scheduler.runTaskLaterAsync(() ->
                DominionSAR.getDominionAPI().getAllDominions().forEach(dominion -> {
            if (dominion.getWorld() == null) return;
            renderDominion(dominion);
        }), 30 * 20L); // Delay initialization by 1 second to ensure all plugins are loaded
    }

    public List<MapProvider> getMapProviders() {
        return mapProviders;
    }

    public static DominionAPI getDominionAPI() {
        if (dominionAPI == null) {
            dominionAPI = DominionAPI.getInstance();
        }
        return dominionAPI;
    }

    public static DominionSAR getInstance() {
        return instance;
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
