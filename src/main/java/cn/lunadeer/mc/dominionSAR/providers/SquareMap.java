package cn.lunadeer.mc.dominionSAR.providers;

import cn.lunadeer.dominion.api.dtos.CuboidDTO;
import cn.lunadeer.mc.dominionSAR.MapProvider;
import org.bukkit.World;
import xyz.jpenilla.squaremap.api.*;
import xyz.jpenilla.squaremap.api.Point;
import xyz.jpenilla.squaremap.api.marker.Marker;
import xyz.jpenilla.squaremap.api.marker.MarkerOptions;

import java.awt.*;

public class SquareMap implements MapProvider {

    private final Squaremap api;

    public SquareMap() {
        api = SquaremapProvider.get();
    }

    @Override
    public void addMarker(int id, World world, String set, String name, String subInfo, CuboidDTO cuboid, Color innerColor, Color borderColor) {
        api.getWorldIfEnabled(BukkitAdapter.worldIdentifier(world)).ifPresent(mapWorld -> {
            SimpleLayerProvider dominionProvider = (SimpleLayerProvider) mapWorld.layerRegistry().get(Key.of(set));

            Point p1 = Point.of(cuboid.x1(), cuboid.z1());
            Point p2 = Point.of(cuboid.x2(), cuboid.z2());

            MarkerOptions options = MarkerOptions.builder()
                    .fillColor(innerColor).fillOpacity(0.2)
                    .strokeColor(borderColor).strokeOpacity(0.8)
                    .build();

            Marker marker = Marker.rectangle(p1, p2).markerOptions(options);

            dominionProvider.removeMarker(Key.of(name));
            dominionProvider.addMarker(Key.of(name), marker);

            // Re-register the layer to apply changes
            mapWorld.layerRegistry().unregister(Key.of(set));
            mapWorld.layerRegistry().register(Key.of(set), dominionProvider);
        });
    }

    @Override
    public void removeMarker(int id, World world, String set, String name) {
        api.getWorldIfEnabled(BukkitAdapter.worldIdentifier(world)).ifPresent(mapWorld -> {
            SimpleLayerProvider dominionProvider = (SimpleLayerProvider) mapWorld.layerRegistry().get(Key.of(set));
            dominionProvider.removeMarker(Key.of(name));
            // Re-register the layer to apply changes
            mapWorld.layerRegistry().unregister(Key.of(set));
            mapWorld.layerRegistry().register(Key.of(set), dominionProvider);
        });
    }
}
