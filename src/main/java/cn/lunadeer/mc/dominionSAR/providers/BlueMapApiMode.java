package cn.lunadeer.mc.dominionSAR.providers;

import cn.lunadeer.dominion.api.dtos.CuboidDTO;
import cn.lunadeer.mc.dominionSAR.MapProvider;
import cn.lunadeer.mc.dominionSAR.providers.bluemap.BlueMapMarkerSupport;
import com.flowpowered.math.vector.Vector2d;
import com.flowpowered.math.vector.Vector3d;
import de.bluecolored.bluemap.api.BlueMapAPI;
import de.bluecolored.bluemap.api.markers.ExtrudeMarker;
import de.bluecolored.bluemap.api.markers.MarkerSet;
import de.bluecolored.bluemap.api.markers.ShapeMarker;
import org.bukkit.World;

import java.awt.*;
import java.util.Collection;
import java.util.List;

public class BlueMapApiMode implements MapProvider {

    private static final float MIN_POLYGON_Y = 255.0F;

    @Override
    public void addMarker(int id, World world, String set, String name, String subInfo, CuboidDTO cuboid, Color innerColor, Color borderColor) {
        BlueMapAPI.getInstance()
                .flatMap(api -> api.getWorld(world)).ifPresent(blueMapWorld -> {
                    blueMapWorld.getMaps().forEach(map -> {
                        BlueMapMarkerSupport.MarkerSetDefinition markerSetDefinition = BlueMapMarkerSupport.createMarkerSet(set);
                        MarkerSet blueMapSet = map.getMarkerSets().get(set);
                        if (blueMapSet == null) {
                            blueMapSet = MarkerSet.builder()
                                    .label(markerSetDefinition.label())
                                    .toggleable(markerSetDefinition.toggleable())
                                    .defaultHidden(markerSetDefinition.defaultHidden())
                                    .sorting(markerSetDefinition.sorting())
                                    .build();
                            map.getMarkerSets().put(set, blueMapSet);
                        }
                        BlueMapMarkerSupport.ExtrudeMarkerDefinition markerDefinition = BlueMapMarkerSupport.createExtrudeMarker(
                                id,
                                name,
                                subInfo,
                                cuboid,
                                innerColor,
                                borderColor
                        );

                        ExtrudeMarker marker = ExtrudeMarker.builder()
                                .label(markerDefinition.label())
                                .detail(markerDefinition.detail())
                                .position(new Vector3d(
                                        markerDefinition.position().x(),
                                        markerDefinition.position().y(),
                                        markerDefinition.position().z()
                                ))
                                .shape(createShape(markerDefinition.shape()), markerDefinition.shapeMinY(), markerDefinition.shapeMaxY())
                                .depthTestEnabled(markerDefinition.depthTest())
                                .lineWidth(markerDefinition.lineWidth())
                                .lineColor(toBlueMapColor(markerDefinition.lineColor()))
                                .fillColor(toBlueMapColor(markerDefinition.fillColor()))
                                .build();
                        blueMapSet.getMarkers().put(String.valueOf(id), marker);
                    });
                });
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
        float shapeY = Math.max(MIN_POLYGON_Y, world.getMaxHeight() - 1.0F);

        BlueMapAPI.getInstance()
                .flatMap(api -> api.getWorld(world)).ifPresent(blueMapWorld -> {
                    blueMapWorld.getMaps().forEach(map -> {
                        BlueMapMarkerSupport.MarkerSetDefinition markerSetDefinition = BlueMapMarkerSupport.createMarkerSet(set);
                        MarkerSet blueMapSet = map.getMarkerSets().get(set);
                        if (blueMapSet == null) {
                            blueMapSet = MarkerSet.builder()
                                    .label(markerSetDefinition.label())
                                    .toggleable(markerSetDefinition.toggleable())
                                    .defaultHidden(markerSetDefinition.defaultHidden())
                                    .sorting(markerSetDefinition.sorting())
                                    .build();
                            map.getMarkerSets().put(set, blueMapSet);
                        }
                        BlueMapMarkerSupport.ShapeMarkerDefinition markerDefinition = BlueMapMarkerSupport.createShapeMarker(
                                id,
                                name,
                                subInfo,
                                vertices,
                                shapeY,
                                innerColor,
                                borderColor
                        );

                        ShapeMarker marker = ShapeMarker.builder()
                                .label(markerDefinition.label())
                                .detail(markerDefinition.detail())
                                .position(new Vector3d(
                                        markerDefinition.position().x(),
                                        markerDefinition.position().y(),
                                        markerDefinition.position().z()
                                ))
                                .shape(createShape(markerDefinition.shape()), markerDefinition.shapeY())
                                .depthTestEnabled(markerDefinition.depthTest())
                                .lineWidth(markerDefinition.lineWidth())
                                .lineColor(toBlueMapColor(markerDefinition.lineColor()))
                                .fillColor(toBlueMapColor(markerDefinition.fillColor()))
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

    private de.bluecolored.bluemap.api.math.Shape createShape(List<BlueMapMarkerSupport.ShapePoint> points) {
        Collection<Vector2d> vectors = points.stream()
                .map(point -> new Vector2d(point.x(), point.z()))
                .toList();
        return new de.bluecolored.bluemap.api.math.Shape(vectors);
    }

    private de.bluecolored.bluemap.api.math.Color toBlueMapColor(BlueMapMarkerSupport.ColorValue color) {
        return new de.bluecolored.bluemap.api.math.Color(
                color.r(),
                color.g(),
                color.b(),
                color.a()
        );
    }
}
