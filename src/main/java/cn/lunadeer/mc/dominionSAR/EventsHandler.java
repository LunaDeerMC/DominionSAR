package cn.lunadeer.mc.dominionSAR;

import cn.lunadeer.dominion.api.dtos.DominionDTO;
import cn.lunadeer.dominion.events.dominion.DominionCreateEvent;
import cn.lunadeer.dominion.events.dominion.DominionDeleteEvent;
import cn.lunadeer.dominion.events.dominion.modify.DominionReSizeEvent;
import cn.lunadeer.dominion.events.dominion.modify.DominionRenameEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.server.ServerLoadEvent;

import java.awt.*;

public class EventsHandler implements Listener {

    private static void renderDominion(DominionDTO dominion) {
        if (!dominion.getEnvFlagValue(DominionSAR.SHOW_ON_WEB)) return;    // Skip if user disabled it
        for (MapProvider provider : DominionSAR.getInstance().getMapProviders()) {
            Color inner = new Color(dominion.getColorR(), dominion.getColorG(), dominion.getColorB(), 40);
            Color border = new Color(dominion.getColorR(), dominion.getColorG(), dominion.getColorB(), 160);
            provider.addMarker(dominion.getId(), dominion.getWorld(), "Dominion",
                    dominion.getName(), dominion.getOwnerDTO().getLastKnownName(), dominion.getCuboid(), inner, border);
        }
    }

    private static void removeDominion(DominionDTO dominion) {
        for (MapProvider provider : DominionSAR.getInstance().getMapProviders()) {
            provider.removeMarker(dominion.getId(), dominion.getWorld(), "Dominion", dominion.getName());
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onDominionCreate(DominionCreateEvent event) {
        if (event.isCancelled()) return;
        event.afterCreated(dominionDTO -> {
            if (dominionDTO == null) return;
            renderDominion(dominionDTO);
        });
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onDominionResize(DominionReSizeEvent event) {
        if (event.isCancelled()) return;
        removeDominion(event.getDominion());
        event.afterModified(newDom -> {
            if (newDom == null) return;
            renderDominion(newDom);
        });
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onDominionRename(DominionRenameEvent event) {
        if (event.isCancelled()) return;
        if (event.getDominion().getWorld() == null) return;
        removeDominion(event.getDominion());
        event.afterModified(newDom -> {
            if (newDom == null) return;
            renderDominion(newDom);
        });
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onDominionDelete(DominionDeleteEvent event) {
        if (event.isCancelled()) return;
        if (event.getDominion().getWorld() == null) return;
        removeDominion(event.getDominion());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onWorldLoaded(ServerLoadEvent event) {
        DominionSAR.getDominionAPI().getAllDominions().forEach(dominion -> {
            if (dominion.getWorld() == null) return;
            renderDominion(dominion);
        });
    }
}
