package cn.lunadeer.mc.dominionSAR.providers;

import cn.lunadeer.dominion.api.dtos.CuboidDTO;
import cn.lunadeer.mc.dominionSAR.MapProvider;
import com.flowpowered.math.vector.Vector2d;
import de.bluecolored.bluemap.api.BlueMapAPI;
import de.bluecolored.bluemap.api.markers.ExtrudeMarker;
import de.bluecolored.bluemap.api.markers.MarkerSet;
import org.bukkit.World;

import java.awt.*;
import java.util.Arrays;
import java.util.Collection;

public class BlueMap implements MapProvider {

    private static final double SHAPE_OFFSET = 0.001;

    @Override
    public void addMarker(int id, World world, String set, String name, String subInfo, CuboidDTO cuboid, Color innerColor, Color borderColor) {
        BlueMapAPI.getInstance()
                .flatMap(api -> api.getWorld(world)).ifPresent(blueMapWorld -> {
                    blueMapWorld.getMaps().forEach(map -> {
                        // get marker set, create if not exists
                        MarkerSet blueMapSet = map.getMarkerSets().get(set);
                        if (blueMapSet == null) {
                            blueMapSet = MarkerSet.builder()
                                    .label(set)
                                    .defaultHidden(true)
                                    .build();
                            map.getMarkerSets().put(set, blueMapSet);
                        }
                        // render content
                        de.bluecolored.bluemap.api.math.Shape shape = createRectangularShape(
                                cuboid.x1(), cuboid.z1(),
                                cuboid.x2(), cuboid.z2()
                        );

                        de.bluecolored.bluemap.api.math.Color lineColor =
                                new de.bluecolored.bluemap.api.math.Color(
                                        innerColor.getRed(),
                                        innerColor.getGreen(),
                                        innerColor.getBlue(),
                                        innerColor.getAlpha() / 255F
                                );
                        de.bluecolored.bluemap.api.math.Color fillColor =
                                new de.bluecolored.bluemap.api.math.Color(
                                        borderColor.getRed(),
                                        borderColor.getGreen(),
                                        borderColor.getBlue(),
                                        borderColor.getAlpha() / 255F
                                );

                        ExtrudeMarker marker = ExtrudeMarker.builder()
                                .label(name)
                                .detail(subInfo)
                                .position(cuboid.x1() + SHAPE_OFFSET, cuboid.y1(), cuboid.z1() + SHAPE_OFFSET)
                                .shape(shape, cuboid.y1() + (float) SHAPE_OFFSET, cuboid.y2() - (float) SHAPE_OFFSET)
                                .lineColor(lineColor)
                                .fillColor(fillColor)
                                .build();
                        blueMapSet.getMarkers().put(String.valueOf(id), marker);
                    });
                });
    }

    @Override
    public void removeMarker(int id, World world, String set, String name) {
        BlueMapAPI.getInstance()
                .flatMap(api -> api.getWorld(world)).ifPresent(blueMapWorld -> {
                    blueMapWorld.getMaps().forEach(map -> {
                        MarkerSet blueMapSet = map.getMarkerSets().get(set);
                        if (blueMapSet == null) return;
                        blueMapSet.getMarkers().remove(String.valueOf(id));
                    });
                });
    }

    private de.bluecolored.bluemap.api.math.Shape createRectangularShape(double x1, double z1, double x2, double z2) {
        Collection<Vector2d> vectors = Arrays.asList(
                new Vector2d(x1 + SHAPE_OFFSET, z1 + SHAPE_OFFSET),
                new Vector2d(x2 - SHAPE_OFFSET, z1 + SHAPE_OFFSET),
                new Vector2d(x2 - SHAPE_OFFSET, z2 - SHAPE_OFFSET),
                new Vector2d(x1 + SHAPE_OFFSET, z2 - SHAPE_OFFSET)
        );
        return new de.bluecolored.bluemap.api.math.Shape(vectors);
    }
}
