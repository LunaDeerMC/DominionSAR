package cn.lunadeer.mc.dominionSAR;

import cn.lunadeer.dominion.api.DominionAPI;
import cn.lunadeer.dominion.api.dtos.flag.EnvFlag;
import cn.lunadeer.dominion.api.dtos.flag.Flags;
import cn.lunadeer.mc.dominionSAR.providers.BlueMapApiMode;
import cn.lunadeer.mc.dominionSAR.providers.BlueMapStandaloneMode;
import cn.lunadeer.mc.dominionSAR.providers.Dynmap;
import cn.lunadeer.mc.dominionSAR.providers.Pl3xMap;
import cn.lunadeer.mc.dominionSAR.providers.SquareMap;
import cn.lunadeer.mc.dominionSAR.utils.configuration.ConfigurationManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

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
        if (Configuration.getBluemapMode() == Configuration.BluemapMode.API) {
            mapProviders.add(new BlueMapApiMode());
        } else if (Configuration.getBluemapMode() == Configuration.BluemapMode.STANDALONE) {
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
