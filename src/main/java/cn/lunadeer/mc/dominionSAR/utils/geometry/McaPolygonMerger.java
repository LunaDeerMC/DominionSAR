package cn.lunadeer.mc.dominionSAR.utils.geometry;

import cn.lunadeer.dominion.utils.McaRecord;

import java.awt.*;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;

public final class McaPolygonMerger {

    public static final int MCA_SIZE = 512;

    private static final Comparator<GridCell> CELL_ORDER = Comparator
            .comparingInt(GridCell::z)
            .thenComparingInt(GridCell::x);

    private McaPolygonMerger() {
    }

    public static List<MergedMcaPolygon> merge(List<McaRecord> mcaRecords) {
        if (mcaRecords == null || mcaRecords.isEmpty()) {
            return List.of();
        }

        Map<String, Set<GridCell>> cellsByWorld = new TreeMap<>();
        for (McaRecord mcaRecord : mcaRecords) {
            if (mcaRecord == null || mcaRecord.world() == null || mcaRecord.world().isBlank()) {
                continue;
            }
            cellsByWorld
                    .computeIfAbsent(mcaRecord.world(), key -> new HashSet<>())
                    .add(new GridCell(mcaRecord.x(), mcaRecord.z()));
        }

        return mergeWorldCells(cellsByWorld);
    }

    static List<MergedMcaPolygon> mergeGridCells(String worldName, Collection<Point> cells) {
        if (worldName == null || worldName.isBlank() || cells == null || cells.isEmpty()) {
            return List.of();
        }

        Map<String, Set<GridCell>> cellsByWorld = new TreeMap<>();
        Set<GridCell> gridCells = cellsByWorld.computeIfAbsent(worldName, key -> new HashSet<>());
        for (Point cell : cells) {
            if (cell == null) {
                continue;
            }
            gridCells.add(new GridCell(cell.x, cell.y));
        }

        return mergeWorldCells(cellsByWorld);
    }

    private static List<MergedMcaPolygon> mergeWorldCells(Map<String, Set<GridCell>> cellsByWorld) {
        if (cellsByWorld.isEmpty()) {
            return List.of();
        }

        List<MergedMcaPolygon> merged = new ArrayList<>();
        for (Map.Entry<String, Set<GridCell>> entry : cellsByWorld.entrySet()) {
            int componentIndex = 0;
            for (Set<GridCell> component : splitConnectedComponents(entry.getValue())) {
                merged.addAll(buildPolygons(entry.getKey(), component, componentIndex++));
            }
        }
        return merged;
    }

    private static List<Set<GridCell>> splitConnectedComponents(Set<GridCell> cells) {
        if (cells.isEmpty()) {
            return List.of();
        }

        Set<GridCell> remaining = new HashSet<>(cells);
        List<Set<GridCell>> components = new ArrayList<>();
        while (!remaining.isEmpty()) {
            GridCell seed = remaining.stream().min(CELL_ORDER).orElseThrow();
            Set<GridCell> component = new HashSet<>();
            ArrayDeque<GridCell> queue = new ArrayDeque<>();
            queue.add(seed);
            remaining.remove(seed);

            while (!queue.isEmpty()) {
                GridCell current = queue.removeFirst();
                component.add(current);
                for (GridCell neighbor : current.neighbors()) {
                    if (remaining.remove(neighbor)) {
                        queue.addLast(neighbor);
                    }
                }
            }
            components.add(component);
        }
        return components;
    }

    private static List<MergedMcaPolygon> buildPolygons(String worldName, Set<GridCell> component, int componentIndex) {
        List<List<GridPoint>> rings = traceBoundaryRings(component);
        ComponentBounds bounds = ComponentBounds.of(component);

        if (rings.size() == 1) {
            return List.of(createPolygon(worldName, component, bounds, componentIndex, 0, 1, rings.get(0)));
        }

        List<RectangleCover> covers = buildRectangleCover(component);
        List<MergedMcaPolygon> polygons = new ArrayList<>(covers.size());
        for (int polygonIndex = 0; polygonIndex < covers.size(); polygonIndex++) {
            polygons.add(createPolygon(worldName, component, bounds, componentIndex, polygonIndex, covers.size(), covers.get(polygonIndex).toPoints()));
        }
        return polygons;
    }

    private static MergedMcaPolygon createPolygon(String worldName,
                                                  Set<GridCell> component,
                                                  ComponentBounds bounds,
                                                  int componentIndex,
                                                  int polygonIndex,
                                                  int polygonCount,
                                                  List<GridPoint> ring) {
        List<Point> vertices = scaleAndNormalize(ring);
        String name;
        String subInfo;

        if (component.size() == 1 && polygonCount == 1) {
            GridCell cell = component.iterator().next();
            name = fileName(cell);
            subInfo = name;
        } else {
            name = "MCA whitelist";
            String suffix = polygonCount > 1 ? " part " + (polygonIndex + 1) + "/" + polygonCount : "";
            subInfo = String.format("%d MCA in r.%d.%d -> r.%d.%d%s",
                    component.size(),
                    bounds.minX(),
                    bounds.minZ(),
                    bounds.maxX(),
                    bounds.maxZ(),
                    suffix);
        }

        int id = Objects.hash(worldName, componentIndex, polygonIndex, vertices);
        return new MergedMcaPolygon(worldName, id, name, subInfo, vertices);
    }

    private static List<Point> scaleAndNormalize(List<GridPoint> ring) {
        List<GridPoint> simplified = simplifyRing(ring);
        List<Point> points = new ArrayList<>(simplified.size());
        for (GridPoint point : simplified) {
            points.add(new Point(point.x() * MCA_SIZE, point.z() * MCA_SIZE));
        }
        return points;
    }

    private static List<GridPoint> simplifyRing(List<GridPoint> ring) {
        if (ring.size() <= 4) {
            return ring;
        }

        List<GridPoint> simplified = new ArrayList<>(ring);
        boolean changed;
        do {
            changed = false;
            List<GridPoint> next = new ArrayList<>(simplified.size());
            for (int index = 0; index < simplified.size(); index++) {
                GridPoint previous = simplified.get((index - 1 + simplified.size()) % simplified.size());
                GridPoint current = simplified.get(index);
                GridPoint following = simplified.get((index + 1) % simplified.size());
                if (isCollinear(previous, current, following)) {
                    changed = true;
                    continue;
                }
                next.add(current);
            }
            simplified = next;
        } while (changed && simplified.size() > 4);

        return simplified;
    }

    private static boolean isCollinear(GridPoint previous, GridPoint current, GridPoint following) {
        int deltaX1 = current.x() - previous.x();
        int deltaZ1 = current.z() - previous.z();
        int deltaX2 = following.x() - current.x();
        int deltaZ2 = following.z() - current.z();
        return deltaX1 * deltaZ2 == deltaZ1 * deltaX2;
    }

    private static List<List<GridPoint>> traceBoundaryRings(Set<GridCell> component) {
        Set<DirectedEdge> boundaryEdges = buildBoundaryEdges(component);
        Map<GridPoint, List<DirectedEdge>> outgoingEdges = new HashMap<>();
        for (DirectedEdge edge : boundaryEdges) {
            outgoingEdges.computeIfAbsent(edge.from(), key -> new ArrayList<>()).add(edge);
        }
        for (List<DirectedEdge> edges : outgoingEdges.values()) {
            edges.sort(Comparator.comparing(DirectedEdge::to, Comparator
                    .comparingInt(GridPoint::z)
                    .thenComparingInt(GridPoint::x)));
        }

        Set<DirectedEdge> remaining = new HashSet<>(boundaryEdges);
        List<List<GridPoint>> rings = new ArrayList<>();
        while (!remaining.isEmpty()) {
            DirectedEdge start = remaining.stream()
                    .min(Comparator.comparing(DirectedEdge::from, Comparator
                                    .comparingInt(GridPoint::z)
                                    .thenComparingInt(GridPoint::x))
                            .thenComparing(DirectedEdge::to, Comparator
                                    .comparingInt(GridPoint::z)
                                    .thenComparingInt(GridPoint::x)))
                    .orElseThrow();
            remaining.remove(start);

            List<GridPoint> ring = new ArrayList<>();
            ring.add(start.from());
            GridPoint currentPoint = start.to();
            while (!currentPoint.equals(start.from())) {
                ring.add(currentPoint);
                DirectedEdge next = nextEdge(remaining, outgoingEdges, currentPoint);
                if (next == null) {
                    throw new IllegalStateException("Failed to trace MCA boundary ring");
                }
                remaining.remove(next);
                currentPoint = next.to();
            }
            rings.add(ring);
        }

        rings.sort(Comparator.comparingLong((List<GridPoint> ring) -> Math.abs(signedArea2(ring))).reversed());
        return rings;
    }

    private static long signedArea2(List<GridPoint> ring) {
        long area = 0;
        for (int index = 0; index < ring.size(); index++) {
            GridPoint current = ring.get(index);
            GridPoint next = ring.get((index + 1) % ring.size());
            area += current.x() * next.z() - next.x() * current.z();
        }
        return area;
    }

    private static DirectedEdge nextEdge(Set<DirectedEdge> remaining,
                                         Map<GridPoint, List<DirectedEdge>> outgoingEdges,
                                         GridPoint currentPoint) {
        List<DirectedEdge> candidates = outgoingEdges.get(currentPoint);
        if (candidates == null) {
            return null;
        }
        for (DirectedEdge candidate : candidates) {
            if (remaining.contains(candidate)) {
                return candidate;
            }
        }
        return null;
    }

    private static Set<DirectedEdge> buildBoundaryEdges(Collection<GridCell> component) {
        Set<DirectedEdge> edges = new LinkedHashSet<>();
        component.stream().sorted(CELL_ORDER).forEach(cell -> {
            GridPoint topLeft = new GridPoint(cell.x(), cell.z());
            GridPoint topRight = new GridPoint(cell.x() + 1, cell.z());
            GridPoint bottomRight = new GridPoint(cell.x() + 1, cell.z() + 1);
            GridPoint bottomLeft = new GridPoint(cell.x(), cell.z() + 1);

            toggleEdge(edges, new DirectedEdge(topLeft, topRight));
            toggleEdge(edges, new DirectedEdge(topRight, bottomRight));
            toggleEdge(edges, new DirectedEdge(bottomRight, bottomLeft));
            toggleEdge(edges, new DirectedEdge(bottomLeft, topLeft));
        });
        return edges;
    }

    private static void toggleEdge(Set<DirectedEdge> edges, DirectedEdge candidate) {
        DirectedEdge reverse = candidate.reverse();
        if (!edges.remove(reverse)) {
            edges.add(candidate);
        }
    }

    private static List<RectangleCover> buildRectangleCover(Set<GridCell> component) {
        Set<GridCell> remaining = new HashSet<>(component);
        List<RectangleCover> rectangles = new ArrayList<>();

        while (!remaining.isEmpty()) {
            GridCell origin = remaining.stream().min(CELL_ORDER).orElseThrow();
            int width = 1;
            while (remaining.contains(new GridCell(origin.x() + width, origin.z()))) {
                width++;
            }

            int height = 1;
            boolean extend = true;
            while (extend) {
                int nextZ = origin.z() + height;
                for (int deltaX = 0; deltaX < width; deltaX++) {
                    if (!remaining.contains(new GridCell(origin.x() + deltaX, nextZ))) {
                        extend = false;
                        break;
                    }
                }
                if (extend) {
                    height++;
                }
            }

            for (int deltaZ = 0; deltaZ < height; deltaZ++) {
                for (int deltaX = 0; deltaX < width; deltaX++) {
                    remaining.remove(new GridCell(origin.x() + deltaX, origin.z() + deltaZ));
                }
            }
            rectangles.add(new RectangleCover(origin.x(), origin.z(), origin.x() + width, origin.z() + height));
        }

        return rectangles;
    }

    private static String fileName(GridCell cell) {
        return "r." + cell.x() + "." + cell.z() + ".mca";
    }

    public record MergedMcaPolygon(String worldName,
                                   int id,
                                   String name,
                                   String subInfo,
                                   List<Point> vertices) {
    }

    private record GridCell(int x, int z) {
        private List<GridCell> neighbors() {
            return List.of(
                    new GridCell(x + 1, z),
                    new GridCell(x - 1, z),
                    new GridCell(x, z + 1),
                    new GridCell(x, z - 1)
            );
        }
    }

    private record GridPoint(int x, int z) {
    }

    private record DirectedEdge(GridPoint from, GridPoint to) {
        private DirectedEdge reverse() {
            return new DirectedEdge(to, from);
        }
    }

    private record RectangleCover(int minX, int minZ, int maxXExclusive, int maxZExclusive) {
        private List<GridPoint> toPoints() {
            return List.of(
                    new GridPoint(minX, minZ),
                    new GridPoint(maxXExclusive, minZ),
                    new GridPoint(maxXExclusive, maxZExclusive),
                    new GridPoint(minX, maxZExclusive)
            );
        }
    }

    private record ComponentBounds(int minX, int minZ, int maxX, int maxZ) {
        private static ComponentBounds of(Set<GridCell> component) {
            int minX = Integer.MAX_VALUE;
            int minZ = Integer.MAX_VALUE;
            int maxX = Integer.MIN_VALUE;
            int maxZ = Integer.MIN_VALUE;
            for (GridCell cell : component) {
                minX = Math.min(minX, cell.x());
                minZ = Math.min(minZ, cell.z());
                maxX = Math.max(maxX, cell.x());
                maxZ = Math.max(maxZ, cell.z());
            }
            return new ComponentBounds(minX, minZ, maxX, maxZ);
        }
    }
}