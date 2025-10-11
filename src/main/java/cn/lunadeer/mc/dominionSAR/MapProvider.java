package cn.lunadeer.mc.dominionSAR;

import cn.lunadeer.dominion.api.dtos.CuboidDTO;
import org.bukkit.World;

import java.awt.*;

public interface MapProvider {

    void addMarker(int id,
                   World world,
                   String set,
                   String name,
                   String subInfo,
                   CuboidDTO cuboid,
                   Color innerColor,
                   Color borderColor);

    void removeMarker(int id, World world, String set, String name);
}
