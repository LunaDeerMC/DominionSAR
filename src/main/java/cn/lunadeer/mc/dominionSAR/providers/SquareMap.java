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

    /**
     * Sanitize a string to be used as a SquareMap Key.
     * SquareMap keys only allow [a-zA-Z0-9._-] characters.
     * 
     * @param input the input string (e.g., dominion name)
     * @param id the dominion ID to ensure uniqueness
     * @return a sanitized string suitable for use as a SquareMap Key
     */
    private String sanitizeKey(String input, int id) {
        if (input == null || input.isEmpty()) {
            return "dominion_" + id;
        }
        // Replace any character that is not [a-zA-Z0-9._-] with an underscore
        String sanitized = input.replaceAll("[^a-zA-Z0-9._-]", "_");
        // Ensure the key is unique by appending the ID
        return sanitized + "_" + id;
    }

    @Override
    public void addMarker(int id, World world, String set, String name, String subInfo, CuboidDTO cuboid, Color innerColor, Color borderColor) {
        api.getWorldIfEnabled(BukkitAdapter.worldIdentifier(world)).ifPresent(mapWorld -> {
            SimpleLayerProvider dominionProvider;
            if (!mapWorld.layerRegistry().hasEntry(Key.of(set))) {
                dominionProvider = SimpleLayerProvider.builder(set).build();
            } else {
                dominionProvider = (SimpleLayerProvider) mapWorld.layerRegistry().get(Key.of(set));
            }

            Point p1 = Point.of(cuboid.x1(), cuboid.z1());
            Point p2 = Point.of(cuboid.x2(), cuboid.z2());

            MarkerOptions options = MarkerOptions.builder()
                    .fillColor(innerColor).fillOpacity(0.2)
                    .strokeColor(borderColor).strokeOpacity(0.8)
                    .build();

            Marker marker = Marker.rectangle(p1, p2).markerOptions(options);

            // Use sanitized key to avoid IllegalArgumentException with non-ASCII characters
            String sanitizedKey = sanitizeKey(name, id);
            dominionProvider.removeMarker(Key.of(sanitizedKey));
            dominionProvider.addMarker(Key.of(sanitizedKey), marker);

            // Re-register the layer to apply changes
            if (mapWorld.layerRegistry().hasEntry(Key.of(set))) {
                mapWorld.layerRegistry().unregister(Key.of(set));
            }
            mapWorld.layerRegistry().register(Key.of(set), dominionProvider);
        });
    }

    @Override
    public void removeMarker(int id, World world, String set, String name) {
        api.getWorldIfEnabled(BukkitAdapter.worldIdentifier(world)).ifPresent(mapWorld -> {
            SimpleLayerProvider dominionProvider = (SimpleLayerProvider) mapWorld.layerRegistry().get(Key.of(set));
            // Use sanitized key to match the key used in addMarker
            String sanitizedKey = sanitizeKey(name, id);
            dominionProvider.removeMarker(Key.of(sanitizedKey));
            // Re-register the layer to apply changes
            mapWorld.layerRegistry().unregister(Key.of(set));
            mapWorld.layerRegistry().register(Key.of(set), dominionProvider);
        });
    }
}
