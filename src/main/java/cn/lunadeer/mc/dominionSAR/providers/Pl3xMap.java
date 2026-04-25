package cn.lunadeer.mc.dominionSAR.providers;

import cn.lunadeer.dominion.api.dtos.CuboidDTO;
import cn.lunadeer.mc.dominionSAR.MapProvider;
import org.bukkit.World;

import java.awt.*;
import java.util.List;

public class Pl3xMap implements MapProvider {
    @Override
    public void addMarker(int id, World world, String set, String name, String subInfo, CuboidDTO cuboid, Color innerColor, Color borderColor) {

    }

    @Override
    public void addPolygonMarker(int id,
                                 World world,
                                 String set,
                                 String name,
                                 String subInfo,
                                 List<Point> vertices,
                                 Color innerColor,
                                 Color borderColor) {

    }

    @Override
    public void removeMarker(int id, World world, String set, String name) {

    }
}
