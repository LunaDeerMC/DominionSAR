package cn.lunadeer.mc.dominionSAR;

import cn.lunadeer.mc.dominionSAR.utils.configuration.Comments;
import cn.lunadeer.mc.dominionSAR.utils.configuration.ConfigurationFile;
import cn.lunadeer.mc.dominionSAR.utils.configuration.ConfigurationPart;

public class Configuration extends ConfigurationFile {

    public static class MapProvider extends ConfigurationPart {
        @Comments("https://bluemap.bluecolored.de/")
        public BlueMap blueMap = new BlueMap();

        @Comments("https://www.spigotmc.org/resources/dynmap%C2%AE.274/")
        public boolean dynmap = false;

        @Comments("https://modrinth.com/plugin/squaremap")
        public boolean squareMap = false;

        @Comments("https://modrinth.com/plugin/pl3xmap")
        public boolean pl3xMap = false;
    }

    public static MapProvider mapProvider = new MapProvider();

    public static class BlueMap extends ConfigurationPart {
        @Comments({
            "disable: Disable the BlueMap integration. This is the default value.",
            "api: If you deploy BlueMap on the same server, you can set this to 'api' to let the plugin hook into BlueMap's API automatically. This is the recommended way to use BlueMap with this plugin, as it will automatically update the markers when the plugin updates them.",
            "standalone: If you deploy BlueMap on a different server, you can set this to 'standalone' to let the plugin save the markers as json files, which can be used by BlueMap's marker API. This is useful if you deploy Bluemap on a different server."
        })
        public String mode = "disable"; // disable, standalone, api

        @Comments("The base directory to save standalone json files. Each world is exported to <path>/<world>/markers.json. It can be an absolute path or a path relative to the server root directory.")
        public String path = "./bluemap-markers";
    }

    public static enum BlueMapMode {
        DISABLE,
        API,
        STANDALONE
    }

    public static BlueMapMode getBluemapMode() {
        return switch (Configuration.mapProvider.blueMap.mode.toLowerCase()) {
            case "api" -> BlueMapMode.API;
            case "standalone" -> BlueMapMode.STANDALONE;
            default -> BlueMapMode.DISABLE;
        };
    }

}
