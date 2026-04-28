package cn.lunadeer.mc.dominionSAR;

import cn.lunadeer.dominion.api.dtos.CuboidDTO;
import cn.lunadeer.dominion.api.dtos.DominionDTO;
import cn.lunadeer.dominion.events.ExportMcaListEvent;
import cn.lunadeer.dominion.events.dominion.DominionCreateEvent;
import cn.lunadeer.dominion.events.dominion.DominionDeleteEvent;
import cn.lunadeer.dominion.events.dominion.modify.DominionReSizeEvent;
import cn.lunadeer.dominion.events.dominion.modify.DominionRenameEvent;
import cn.lunadeer.dominion.utils.McaRecord;
import cn.lunadeer.mc.dominionSAR.utils.geometry.McaPolygonMerger;
import cn.lunadeer.mc.dominionSAR.utils.scheduler.Scheduler;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class EventsHandler implements Listener {

    private static final String MCA_MARKER_SET = "MCA-whitelist";
    private static final Object MCA_MARKER_LOCK = new Object();
    private static List<RenderedMcaMarker> renderedMcaMarkers = List.of();

    public static void renderDominion(DominionDTO dominion) {
        if (!dominion.getEnvFlagValue(DominionSAR.SHOW_ON_WEB))
            return; // Skip if user disabled it
        for (MapProvider provider : DominionSAR.getInstance().getMapProviders()) {
            Color inner = new Color(dominion.getColorR(), dominion.getColorG(), dominion.getColorB(), 40);
            Color border = new Color(dominion.getColorR(), dominion.getColorG(), dominion.getColorB(), 160);
            provider.addMarker(dominion.getId(), dominion.getWorld(), "Dominion",
                    dominion.getName(),
                    Configuration.labelInfo.replace("%dominion_name%", dominion.getName()).replace("%owner%",
                            dominion.getOwnerDTO().getLastKnownName()),
                    dominion.getCuboid(), inner, border);
        }
    }

    private static void renderMcaSquare(List<McaRecord> mcaRecords) {
        Color inner = new Color(177, 255, 118, 118);
        Color border = new Color(9, 85, 18, 255);
        List<McaPolygonMerger.MergedMcaPolygon> mergedPolygons = McaPolygonMerger.merge(mcaRecords);

        synchronized (MCA_MARKER_LOCK) {
            clearRenderedMcaMarkers();
            List<RenderedMcaMarker> newMarkers = new ArrayList<>();
            for (McaPolygonMerger.MergedMcaPolygon polygon : mergedPolygons) {
                World bukkitWorld = DominionSAR.getInstance().getServer().getWorld(polygon.worldName());
                if (bukkitWorld == null) {
                    DominionSAR.getInstance().getLogger().warning("Skipping MCA whitelist render for unloaded world: " + polygon.worldName());
                    continue;
                }

                for (MapProvider provider : DominionSAR.getInstance().getMapProviders()) {
                    provider.addPolygonMarker(polygon.id(), bukkitWorld, MCA_MARKER_SET,
                            polygon.name(), polygon.subInfo(),
                            polygon.vertices(),
                            inner, border);
                }
                newMarkers.add(new RenderedMcaMarker(polygon.id(), polygon.worldName(), polygon.name()));
            }

            renderedMcaMarkers = newMarkers;
        }
    }

    private static void clearRenderedMcaMarkers() {
        for (RenderedMcaMarker marker : renderedMcaMarkers) {
            World bukkitWorld = DominionSAR.getInstance().getServer().getWorld(marker.worldName());
            if (bukkitWorld == null) {
                continue;
            }
            for (MapProvider provider : DominionSAR.getInstance().getMapProviders()) {
                provider.removeMarker(marker.id(), bukkitWorld, MCA_MARKER_SET, marker.name());
            }
        }
    }

    private static void removeDominion(DominionDTO dominion) {
        for (MapProvider provider : DominionSAR.getInstance().getMapProviders()) {
            provider.removeMarker(dominion.getId(), dominion.getWorld(), "Dominion", dominion.getName());
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onDominionCreate(DominionCreateEvent event) {
        if (event.isCancelled())
            return;
        event.afterCreated(dominionDTO -> {
            if (dominionDTO == null)
                return;
            renderDominion(dominionDTO);
        });
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onDominionResize(DominionReSizeEvent event) {
        if (event.isCancelled())
            return;
        removeDominion(event.getDominion());
        event.afterModified(newDom -> {
            if (newDom == null)
                return;
            renderDominion(newDom);
        });
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onDominionRename(DominionRenameEvent event) {
        if (event.isCancelled())
            return;
        if (event.getDominion().getWorld() == null)
            return;
        removeDominion(event.getDominion());
        event.afterModified(newDom -> {
            if (newDom == null)
                return;
            renderDominion(newDom);
        });
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onDominionDelete(DominionDeleteEvent event) {
        if (event.isCancelled())
            return;
        if (event.getDominion().getWorld() == null)
            return;
        removeDominion(event.getDominion());
    }

    @EventHandler
    public void onExportedMca(ExportMcaListEvent event) {
        if (!Configuration.renderMcaWhiteList)
            return;
        Scheduler.runTaskAsync(() -> {
            renderMcaSquare(event.getList());
        });
    }

    private record RenderedMcaMarker(int id, String worldName, String name) {
    }
}
