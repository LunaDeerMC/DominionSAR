package cn.lunadeer.mc.dominionSAR.providers.bluemap;

import cn.lunadeer.dominion.api.dtos.CuboidDTO;

import java.awt.Color;
import java.util.List;

public final class BlueMapMarkerSupport {

    public static final double SHAPE_OFFSET = 0.001D;
    public static final int DEFAULT_LINE_WIDTH = 2;
    public static final String MARKERS_FILE_NAME = "markers.json";

    private BlueMapMarkerSupport() {
    }

    public static MarkerSetDefinition createMarkerSet(String label) {
        return new MarkerSetDefinition(label, true, true, 0);
    }

    public static ExtrudeMarkerDefinition createExtrudeMarker(int id,
                                                              String name,
                                                              String detail,
                                                              CuboidDTO cuboid,
                                                              Color fillColor,
                                                              Color lineColor) {
        Bounds bounds = createBounds(cuboid);

        return new ExtrudeMarkerDefinition(
                String.valueOf(id),
                "extrude",
                new Position(
                        (bounds.minX + bounds.maxX) / 2D,
                        (bounds.minY + bounds.maxY) / 2D,
                        (bounds.minZ + bounds.maxZ) / 2D
                ),
                name,
                List.of(
                        new ShapePoint(bounds.minX, bounds.minZ),
                        new ShapePoint(bounds.maxX, bounds.minZ),
                        new ShapePoint(bounds.maxX, bounds.maxZ),
                        new ShapePoint(bounds.minX, bounds.maxZ)
                ),
                List.of(),
                bounds.minY,
                bounds.maxY,
                detail,
                false,
                true,
                DEFAULT_LINE_WIDTH,
                toColorValue(lineColor),
                toColorValue(fillColor),
                0,
                true
        );
    }

    private static Bounds createBounds(CuboidDTO cuboid) {
        double minX = insetMin(cuboid.x1(), cuboid.x2());
        double maxX = insetMax(cuboid.x1(), cuboid.x2());
        double minY = insetMin(cuboid.y1(), cuboid.y2());
        double maxY = insetMax(cuboid.y1(), cuboid.y2());
        double minZ = insetMin(cuboid.z1(), cuboid.z2());
        double maxZ = insetMax(cuboid.z1(), cuboid.z2());
        return new Bounds(minX, maxX, (float) minY, (float) maxY, minZ, maxZ);
    }

    private static double insetMin(double first, double second) {
        double min = Math.min(first, second);
        double max = Math.max(first, second);
        return max - min > SHAPE_OFFSET * 2D ? min + SHAPE_OFFSET : min;
    }

    private static double insetMax(double first, double second) {
        double min = Math.min(first, second);
        double max = Math.max(first, second);
        return max - min > SHAPE_OFFSET * 2D ? max - SHAPE_OFFSET : max;
    }

    private static ColorValue toColorValue(Color color) {
        return new ColorValue(
                color.getRed(),
                color.getGreen(),
                color.getBlue(),
                color.getAlpha() / 255F
        );
    }

    public record MarkerSetDefinition(String label, boolean toggleable, boolean defaultHidden, int sorting) {
    }

    public record ExtrudeMarkerDefinition(String id,
                                          String type,
                                          Position position,
                                          String label,
                                          List<ShapePoint> shape,
                                          List<List<ShapePoint>> holes,
                                          float shapeMinY,
                                          float shapeMaxY,
                                          String detail,
                                          boolean newTab,
                                          boolean depthTest,
                                          int lineWidth,
                                          ColorValue lineColor,
                                          ColorValue fillColor,
                                          int sorting,
                                          boolean listed) {
    }

    public record Position(double x, double y, double z) {
    }

    public record ShapePoint(double x, double z) {
    }

    public record ColorValue(int r, int g, int b, float a) {
    }

    private record Bounds(double minX, double maxX, float minY, float maxY, double minZ, double maxZ) {
    }
}