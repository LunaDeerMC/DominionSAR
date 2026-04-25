package cn.lunadeer.mc.dominionSAR.providers;

import cn.lunadeer.dominion.api.dtos.CuboidDTO;
import cn.lunadeer.mc.dominionSAR.MapProvider;
import org.bukkit.World;
import org.dynmap.DynmapCommonAPI;
import org.dynmap.DynmapCommonAPIListener;
import org.dynmap.markers.AreaMarker;
import org.dynmap.markers.Marker;
import org.dynmap.markers.MarkerAPI;
import org.dynmap.markers.MarkerSet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.util.List;

public class Dynmap extends DynmapCommonAPIListener implements MapProvider {

    private static final double DEFAULT_FILL_OPACITY = 0.2;
    private static final double DEFAULT_LINE_OPACITY = 0.8;
    private static final int DEFAULT_LINE_WEIGHT = 1;

    private MarkerAPI markerAPI;

    public Dynmap() {
        DynmapCommonAPIListener.register(this);
    }

    @Nullable
    private MarkerSet getOrCreateMarkerSet(@NotNull String id, @NotNull String label) {
        MarkerSet markerSet = markerAPI.getMarkerSet(id);
        if (markerSet == null) {
            markerSet = markerAPI.createMarkerSet(id, label, null, false);
        }
        return markerSet;
    }

    @Override
    public void addMarker(int id, World world, String set, String name, String subInfo, CuboidDTO cuboid, Color innerColor, Color borderColor) {
        double[] xCoordinates = {cuboid.x1(), cuboid.x2()};
        double[] zCoordinates = {cuboid.z1(), cuboid.z2()};

        MarkerSet markerSet = getOrCreateMarkerSet(set, set);
        if (markerSet != null) {
            AreaMarker marker = markerSet.createAreaMarker(
                    name,
                    name + "(" + subInfo + ")",
                    true,
                    world.getName(),
                    xCoordinates,
                    zCoordinates,
                    false
            );
            // Apply styling
            marker.setFillStyle(DEFAULT_FILL_OPACITY, innerColor.getRGB());
            marker.setLineStyle(DEFAULT_LINE_WEIGHT, DEFAULT_LINE_OPACITY, borderColor.getRGB());
        }
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
        double[] xCoordinates = vertices.stream().mapToDouble(Point::getX).toArray();
        double[] zCoordinates = vertices.stream().mapToDouble(Point::getY).toArray();

        MarkerSet markerSet = getOrCreateMarkerSet(set, set);
        if (markerSet != null) {
            AreaMarker marker = markerSet.createAreaMarker(
                    String.valueOf(id),
                    name + "(" + subInfo + ")",
                    true,
                    world.getName(),
                    xCoordinates,
                    zCoordinates,
                    false
            );
            marker.setFillStyle(DEFAULT_FILL_OPACITY, innerColor.getRGB());
            marker.setLineStyle(DEFAULT_LINE_WEIGHT, DEFAULT_LINE_OPACITY, borderColor.getRGB());
        }
    }

    @Override
    public void removeMarker(int id, World world, String set, String name) {
        MarkerSet markerSet = getOrCreateMarkerSet(set, set);
        if (markerSet == null) return;
        Marker marker = markerSet.findMarkerByLabel(name);
        if (marker == null) return;
        marker.deleteMarker();
    }

    @Override
    public void apiEnabled(DynmapCommonAPI dynmapCommonAPI) {
        this.markerAPI = dynmapCommonAPI.getMarkerAPI();
    }
}
