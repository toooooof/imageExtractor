package net.toooooof;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

public class Zone {

    private Set<Integer> coords;

    private int minX;
    private int minY;
    private int maxX;
    private int maxY;

    private Serie top;
    private Serie bottom;
    private Serie left;
    private Serie right;

    private int originalWidth;
    private double angle;


    public Zone(Set<Integer> coords, int width) {
        this.coords = coords;
        this.originalWidth = width;

        minX = coords.stream().min(Comparator.comparingInt(o -> o % width)).orElse(-1) % width;
        maxX = coords.stream().max(Comparator.comparingInt(o -> o % width)).orElse(-1) % width;
        minY = (int) Math.floor(coords.stream().min(Comparator.comparingInt(o -> (int) Math.floor(o / width))).orElse(-1) / width);
        maxY = (int) Math.floor(coords.stream().max(Comparator.comparingInt(o -> (int) Math.floor(o / width))).orElse(-1) / width);

        findBoundingRectangle();

        System.out.println("    -- new zone found : " + this.toString());

    }

    /**
     * Scan bottom and top in order to localize the image border
     */
    private void findBoundingRectangle() {
        int scanWidth = (int) Math.floor(((double) (maxX - minX)) * Extractor.search_boundaries_percentage / 2d);
        int startW = minX + scanWidth;
        int endW = maxX - scanWidth;
        List<Integer> lBottom = new ArrayList<>();
        List<Integer> lTop = new ArrayList<>();

        for (int x = startW ; x < endW ; x++) {
            lBottom.add(firstBlackCoordUp(x));
            lTop.add(firstBlackCoordDown(x));
        }

        bottom = new Serie(lBottom, originalWidth);
        boolean descendingBottom = bottom.isDescending();
        double oppositeBottom = descendingBottom ? Math.abs(bottom.y(minX) - maxY) : Math.abs(bottom.y(maxX) - maxY);
        int coordBottomX = (int) bottom.x(maxY);
        double adjacentBottom = descendingBottom ? coordBottomX - minX : maxX - coordBottomX;
        double angleBottom = Math.atan(oppositeBottom / adjacentBottom);
        if (descendingBottom) angleBottom = -angleBottom;

        top = new Serie(lTop, originalWidth);
        boolean descendingTop = top.isDescending();
        double oppositeTop = descendingTop ? Math.abs(top.y(maxX) - minY) : Math.abs(top.y(minX) - minY);
        int coordTopX = (int) top.x(minY);
        double adjacentTop = descendingTop ? maxX - coordTopX: coordTopX - minX;
        double angleTop = Math.atan(oppositeTop / adjacentTop);
        if (descendingTop) angleTop = -angleTop;

        this.angle = (angleBottom + angleTop) / 2;

        System.out.println("     ===> " +minX + " - "  + bottom.y(minX) + " / " + maxX + " - "  + bottom.affine(maxX));

        System.out.println(Math.toDegrees(angleBottom) + "° / " + Math.toDegrees(angleTop) + "° : " + Math.toDegrees(angle) + "°");

    }

    private int firstBlackCoordUp(int x) {
        int cpt = 0;
        while (cpt < 1000000) {
            int c = x + (maxY - cpt++) * originalWidth;
            if (coords.contains(c)) {
                return c;
            }
        }
        return -1;
    }

    private int firstBlackCoordDown(int x) {
        int cpt = 0;
        while (cpt < 1000000) {
            int c = x + (minY + cpt++) * originalWidth;
            if (coords.contains(c)) {
                return c;
            }
        }
        return -1;
    }

    @Override
    public String toString() {
        return "Zone{" +
                "minX=" + minX +
                ", minY=" + minY +
                ", maxX=" + maxX +
                ", maxY=" + maxY +
                ", pixels=" + coords.size() +
                '}';
    }

    public boolean belong(int x, int y) {
        // TODO compute bounding box
        return coords.contains(x + y * originalWidth);
    }

    public int getWidth() {
        return maxX - minX;
    }

    public int getHeight() {
        return maxY - minY;
    }

    public int getX() {
        return minX;
    }

    public int getY() {
        return minY;
    }

    public double getAngle() {
        return angle;
    }
}
