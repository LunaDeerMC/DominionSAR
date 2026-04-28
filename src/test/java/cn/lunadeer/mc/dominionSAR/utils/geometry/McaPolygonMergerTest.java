package cn.lunadeer.mc.dominionSAR.utils.geometry;

import org.junit.jupiter.api.Test;

import java.awt.*;
import java.util.List;

import static cn.lunadeer.mc.dominionSAR.utils.geometry.McaPolygonMerger.MergedMcaPolygon;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class McaPolygonMergerTest {

    @Test
    void mergesSingleCellIntoOriginalFileMarker() {
        List<MergedMcaPolygon> polygons = McaPolygonMerger.mergeGridCells("world",
                List.of(new Point(2, 3)));

        assertEquals(1, polygons.size());
        assertEquals("r.2.3.mca", polygons.getFirst().name());
        assertEquals(4, polygons.getFirst().vertices().size());
        assertEquals(1, coveredCellArea(polygons));
    }

    @Test
    void mergesStraightStripIntoSingleRectangle() {
        List<MergedMcaPolygon> polygons = McaPolygonMerger.mergeGridCells("world",
                List.of(new Point(0, 0), new Point(1, 0), new Point(2, 0)));

        assertEquals(1, polygons.size());
        assertEquals(4, polygons.getFirst().vertices().size());
        assertEquals(3, coveredCellArea(polygons));
    }

    @Test
    void keepsConcaveShapeAsSingleOuterContour() {
        List<MergedMcaPolygon> polygons = McaPolygonMerger.mergeGridCells("world",
                List.of(new Point(0, 0), new Point(1, 0), new Point(0, 1)));

        assertEquals(1, polygons.size());
        assertEquals(6, polygons.getFirst().vertices().size());
        assertEquals(3, coveredCellArea(polygons));
    }

    @Test
    void separatesDisconnectedOrDiagonalOnlyRegions() {
        List<MergedMcaPolygon> polygons = McaPolygonMerger.mergeGridCells("world",
                List.of(new Point(0, 0), new Point(1, 1), new Point(5, 5)));

        assertEquals(3, polygons.size());
        assertTrue(polygons.stream().allMatch(polygon -> polygon.vertices().size() == 4));
        assertEquals(3, coveredCellArea(polygons));
    }

    @Test
    void decomposesHoleShapeIntoMultipleSimplePolygons() {
        List<MergedMcaPolygon> polygons = McaPolygonMerger.mergeGridCells("world",
                List.of(
                        new Point(0, 0), new Point(1, 0), new Point(2, 0),
                        new Point(0, 1), new Point(2, 1),
                        new Point(0, 2), new Point(1, 2), new Point(2, 2)
                ));

        assertTrue(polygons.size() > 1);
        assertEquals(8, coveredCellArea(polygons));
    }

    private static long coveredCellArea(List<MergedMcaPolygon> polygons) {
        long cellArea = (long) McaPolygonMerger.MCA_SIZE * McaPolygonMerger.MCA_SIZE;
        long area = polygons.stream()
                .mapToLong(polygon -> Math.abs(doubleArea(polygon.vertices())) / 2)
                .sum();
        return area / cellArea;
    }

    private static long doubleArea(List<Point> vertices) {
        long area = 0;
        for (int index = 0; index < vertices.size(); index++) {
            Point current = vertices.get(index);
            Point next = vertices.get((index + 1) % vertices.size());
            area += (long) current.x * next.y - (long) next.x * current.y;
        }
        return area;
    }
}