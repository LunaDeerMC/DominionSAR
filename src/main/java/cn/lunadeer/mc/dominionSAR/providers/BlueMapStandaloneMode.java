package cn.lunadeer.mc.dominionSAR.providers;

import cn.lunadeer.dominion.api.dtos.CuboidDTO;
import cn.lunadeer.mc.dominionSAR.Configuration;
import cn.lunadeer.mc.dominionSAR.DominionSAR;
import cn.lunadeer.mc.dominionSAR.MapProvider;
import cn.lunadeer.mc.dominionSAR.providers.bluemap.BlueMapMarkerSupport;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.bukkit.World;

import java.awt.Color;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.LinkedHashMap;
import java.util.Map;

public class BlueMapStandaloneMode implements MapProvider {

    private final Gson gson = new GsonBuilder()
            .setPrettyPrinting()
            .disableHtmlEscaping()
            .create();
    private final Map<String, Map<String, StandaloneMarkerSet>> markersByWorld = new LinkedHashMap<>();
    private final Path outputDirectory;

    public BlueMapStandaloneMode() {
        this.outputDirectory = resolveOutputDirectory();
        DominionSAR.getInstance().getLogger().info("BlueMap standalone markers will be written to " + outputDirectory);
    }

    @Override
    public synchronized void addMarker(int id,
                                       World world,
                                       String set,
                                       String name,
                                       String subInfo,
                                       CuboidDTO cuboid,
                                       Color innerColor,
                                       Color borderColor) {
        Map<String, StandaloneMarkerSet> worldMarkerSets = markersByWorld.computeIfAbsent(world.getName(), key -> new LinkedHashMap<>());
        StandaloneMarkerSet markerSet = worldMarkerSets.computeIfAbsent(set, key -> new StandaloneMarkerSet(BlueMapMarkerSupport.createMarkerSet(set)));
        BlueMapMarkerSupport.ExtrudeMarkerDefinition marker = BlueMapMarkerSupport.createExtrudeMarker(
                id,
                name,
                subInfo,
                cuboid,
                innerColor,
                borderColor
        );
        markerSet.markers.put(marker.id(), marker);
        flushWorld(world.getName(), worldMarkerSets);
    }

    @Override
    public synchronized void removeMarker(int id, World world, String set, String name) {
        Map<String, StandaloneMarkerSet> worldMarkerSets = markersByWorld.computeIfAbsent(world.getName(), key -> new LinkedHashMap<>());
        StandaloneMarkerSet markerSet = worldMarkerSets.get(set);
        if (markerSet != null) {
            markerSet.markers.remove(String.valueOf(id));
            if (markerSet.markers.isEmpty()) {
                worldMarkerSets.remove(set);
            }
        }
        flushWorld(world.getName(), worldMarkerSets);
    }

    private void flushWorld(String worldName, Map<String, StandaloneMarkerSet> markerSets) {
        Path markerFile = outputDirectory.resolve(worldName).resolve(BlueMapMarkerSupport.MARKERS_FILE_NAME);
        try {
            Files.createDirectories(markerFile.getParent());
            Path tempFile = Files.createTempFile(markerFile.getParent(), "markers-", ".json.tmp");
            Files.writeString(tempFile, gson.toJson(markerSets), StandardCharsets.UTF_8);
            moveIntoPlace(tempFile, markerFile);
        } catch (IOException e) {
            DominionSAR.getInstance().getLogger().warning("Failed to write BlueMap standalone markers for world '" + worldName + "': " + e.getMessage());
        }
    }

    private void moveIntoPlace(Path tempFile, Path targetFile) throws IOException {
        try {
            Files.move(tempFile, targetFile, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (AtomicMoveNotSupportedException ignored) {
            Files.move(tempFile, targetFile, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    private Path resolveOutputDirectory() {
        return Path.of(Configuration.mapProvider.blueMap.path).toAbsolutePath().normalize();
    }

    private static final class StandaloneMarkerSet {
        private final String label;
        private final boolean toggleable;
        private final boolean defaultHidden;
        private final int sorting;
        private final Map<String, BlueMapMarkerSupport.ExtrudeMarkerDefinition> markers = new LinkedHashMap<>();

        private StandaloneMarkerSet(BlueMapMarkerSupport.MarkerSetDefinition markerSet) {
            this.label = markerSet.label();
            this.toggleable = markerSet.toggleable();
            this.defaultHidden = markerSet.defaultHidden();
            this.sorting = markerSet.sorting();
        }
    }
}