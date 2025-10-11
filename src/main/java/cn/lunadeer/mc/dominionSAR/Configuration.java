package cn.lunadeer.mc.dominionSAR;

import cn.lunadeer.mc.dominionSAR.utils.configuration.Comments;
import cn.lunadeer.mc.dominionSAR.utils.configuration.ConfigurationFile;
import cn.lunadeer.mc.dominionSAR.utils.configuration.ConfigurationPart;

public class Configuration extends ConfigurationFile {

    public static class MapProvider extends ConfigurationPart {
        @Comments("https://bluemap.bluecolored.de/")
        public boolean blueMap = false;

        @Comments("https://www.spigotmc.org/resources/dynmap%C2%AE.274/")
        public boolean dynmap = false;

        @Comments("https://modrinth.com/plugin/squaremap")
        public boolean squareMap = false;

        @Comments("https://modrinth.com/plugin/pl3xmap")
        public boolean pl3xMap = false;
    }

    public static MapProvider mapProvider = new MapProvider();

}
