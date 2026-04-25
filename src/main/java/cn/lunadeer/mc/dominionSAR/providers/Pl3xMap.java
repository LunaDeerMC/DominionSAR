package cn.lunadeer.mc.dominionSAR.providers;

import cn.lunadeer.dominion.api.dtos.CuboidDTO;
import cn.lunadeer.mc.dominionSAR.MapProvider;
import net.pl3x.map.core.markers.marker.Marker;
import net.pl3x.map.core.markers.option.Options;
import net.pl3x.map.core.registry.Registry;
import org.bukkit.World;

import java.awt.*;
import java.util.List;
import java.util.Optional;

public class Pl3xMap implements MapProvider {

    private static final double FILL_OPACITY = 0.2D;
    private static final double STROKE_OPACITY = 0.8D;
    private static final int STROKE_WEIGHT = 2;

    private final net.pl3x.map.core.Pl3xMap api;

    public Pl3xMap() {
        this.api = net.pl3x.map.core.Pl3xMap.api();
    }

    private String sanitizeKey(String input, int id) {
        if (input == null || input.isEmpty()) {
            return "dominion_" + id;
        }
        String sanitized = input.replaceAll("[^a-zA-Z0-9._-]", "_");
        sanitized = sanitized.replaceAll("_+", "_");
        sanitized = sanitized.replaceAll("^_+|_+$", "");
        if (sanitized.isEmpty()) {
            sanitized = "dominion";
        }
        return sanitized + "_" + id;
    }

    private Optional<net.pl3x.map.core.world.World> getWorld(World world) {
        if (this.api == null || world == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(this.api.getWorldRegistry().get(world.getName()));
    }

    private net.pl3x.map.core.markers.layer.SimpleLayer getOrCreateLayer(net.pl3x.map.core.world.World world, String set) {
        Registry<net.pl3x.map.core.markers.layer.Layer> registry = world.getLayerRegistry();
        net.pl3x.map.core.markers.layer.Layer existing = registry.get(set);
        if (existing instanceof net.pl3x.map.core.markers.layer.SimpleLayer layer) {
            return layer;
        }

        net.pl3x.map.core.markers.layer.SimpleLayer layer = new net.pl3x.map.core.markers.layer.SimpleLayer(set, () -> set);
        registry.register(set, layer);
        return layer;
    }

    private Options buildOptions(String name, String subInfo, Color innerColor, Color borderColor) {
        StringBuilder tooltip = new StringBuilder(name);
        if (subInfo != null && !subInfo.isBlank()) {
            tooltip.append("\n").append(subInfo);
        }

        return Options.builder()
                .fill(true)
                .fillColor(withOpacity(innerColor, FILL_OPACITY))
                .stroke(true)
                .strokeColor(withOpacity(borderColor, STROKE_OPACITY))
                .strokeWeight(STROKE_WEIGHT)
                .tooltipContent(tooltip.toString())
                .popupContent(subInfo)
                .build();
    }

    private int withOpacity(Color color, double opacity) {
        int alpha = (int) Math.round(Math.max(0, Math.min(1, opacity)) * 255);
        return (alpha << 24) | (color.getRGB() & 0x00FFFFFF);
    }

    @Override
    public void addMarker(int id, World world, String set, String name, String subInfo, CuboidDTO cuboid, Color innerColor, Color borderColor) {
        getWorld(world).ifPresent(mapWorld -> {
            String key = sanitizeKey(name, id);
            net.pl3x.map.core.markers.layer.SimpleLayer layer = getOrCreateLayer(mapWorld, set);
            layer.removeMarker(key);
            layer.addMarker(Marker.rectangle(key, cuboid.x1(), cuboid.z1(), cuboid.x2(), cuboid.z2())
                    .setOptions(buildOptions(name, subInfo, innerColor, borderColor)));
            mapWorld.getLayerRegistry().register(set, layer);
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
        getWorld(world).ifPresent(mapWorld -> {
            String key = sanitizeKey(name, id);
            net.pl3x.map.core.markers.layer.SimpleLayer layer = getOrCreateLayer(mapWorld, set);
            layer.removeMarker(key);

            List<net.pl3x.map.core.markers.Point> points = vertices.stream()
                    .map(vertex -> net.pl3x.map.core.markers.Point.of(vertex.getX(), vertex.getY()))
                    .toList();

            layer.addMarker(Marker.polygon(key, Marker.polyline(key, points))
                    .setOptions(buildOptions(name, subInfo, innerColor, borderColor)));
            mapWorld.getLayerRegistry().register(set, layer);
        });
    }

    @Override
    public void removeMarker(int id, World world, String set, String name) {
        getWorld(world).ifPresent(mapWorld -> {
            net.pl3x.map.core.markers.layer.Layer existing = mapWorld.getLayerRegistry().get(set);
            if (!(existing instanceof net.pl3x.map.core.markers.layer.SimpleLayer layer)) {
                return;
            }

            layer.removeMarker(sanitizeKey(name, id));
            mapWorld.getLayerRegistry().register(set, layer);
        });
    }
}
