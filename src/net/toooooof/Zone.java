package net.toooooof;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

public class Zone {

    private final Set<Integer> coords;

    private final int minX;
    private final int minY;
    private final int maxX;
    private final int maxY;

    private Serie top;
    private Serie bottom;
    private Serie left;
    private Serie right;

    private final int originalWidth;
    private double angle;
    private Serie.Point topLeft;
    private Serie.Point bottomRight;

    private String fileName;

    private final Extractor.Conf conf;

    public Zone(Set<Integer> coords, int width, Extractor.Conf conf) {
        this.coords = coords;
        this.originalWidth = width;
        this.conf = conf;

        minX = coords.stream().min(Comparator.comparingInt(o -> o % width)).orElse(-1) % width;
        maxX = coords.stream().max(Comparator.comparingInt(o -> o % width)).orElse(-1) % width;
        minY = (int) Math.floor(coords.stream().min(Comparator.comparingInt(o -> (int) Math.floor(o / width))).orElse(-1) / width);
        maxY = (int) Math.floor(coords.stream().max(Comparator.comparingInt(o -> (int) Math.floor(o / width))).orElse(-1) / width);

        findBoundingRectangle();

        System.out.println("    -- new zone found : " + this);

        computeCropDistances();

    }

    private void computeCropDistances() {
        var x1 = 0;
        var x2 = 0;
        var y1 = 0;
        var y2 = 0;
        if (this.top.isDescending()) {
            x1 = (int) intersection(left, top);
            x2 = maxX;
        } else {
            x1 = minX;
            x2 = (int) intersection(right, top);
        }

        var distance = Math.abs(x2 - x1);
        var verticalDistance = (int) Math.sin(angle) * distance;
        System.out.println("      -- Vertical crop distance: " + (maxY - minY - verticalDistance*2));
    }

    /**
     * Scan bottom and top in order to localize the image border
     */
    private void findBoundingRectangle() {
        int scanWidth = (int) Math.floor((maxX - minX) * conf.getSearchBoundariesPercentage() / 2d);
        int startW = minX + scanWidth;
        int endW = maxX - scanWidth;
        int scanHeight = (int) Math.floor((maxY - minY) * conf.getSearchBoundariesPercentage() / 2d);
        int startH = minY + scanHeight;
        int endH = maxY - scanHeight;

        List<Integer> lBottom = new ArrayList<>();
        List<Integer> lTop = new ArrayList<>();
        List<Integer> lLeft = new ArrayList<>();
        List<Integer> lRight = new ArrayList<>();

        for (int x = startW ; x < endW ; x++) {
            lBottom.add(firstBlackCoordUp(x));
            lTop.add(firstBlackCoordDown(x));
        }

        for (int y = startH ; y < endH ; y++) {
            lLeft.add(firstBlackCoordRight(y));
            lRight.add(firstBlackCoordLeft(y));
        }

        bottom = new Serie(lBottom, originalWidth);
        boolean descendingBottom = bottom.isDescending();
        double oppositeBottom = descendingBottom ? Math.abs(bottom.y(minX) - maxY) : Math.abs(bottom.y(maxX) - maxY);
        int coordBottomX = (int) bottom.x(maxY);
        double adjacentBottom = descendingBottom ? coordBottomX - minX : maxX - coordBottomX;
        double angleBottom = Math.atan(oppositeBottom / adjacentBottom);
        if (!descendingBottom) angleBottom = -angleBottom;

        top = new Serie(lTop, originalWidth);
        boolean descendingTop = top.isDescending();
        double oppositeTop = descendingTop ? Math.abs(top.y(maxX) - minY) : Math.abs(top.y(minX) - minY);
        int coordTopX = (int) top.x(minY);
        double adjacentTop = descendingTop ? maxX - coordTopX: coordTopX - minX;
        double angleTop = Math.atan(oppositeTop / adjacentTop);
        if (!descendingTop) angleTop = -angleTop;

        this.angle = (angleBottom + angleTop) / 2;

        double a = Math.atan(this.angle);

        left = new Serie(lLeft, originalWidth, -1/a);
        right = new Serie(lRight, originalWidth, -1/a);

        double xintersection = intersection(left, top);
        double yintersection = top.y(xintersection);

        topLeft = new Serie.Point((int) xintersection, (int) yintersection);

        xintersection = intersection(right, bottom);
        yintersection = bottom.y(xintersection);

        bottomRight = new Serie.Point((int) xintersection, (int) yintersection);

        System.out.println("     ===> " +minX + " - "  + bottom.y(minX) + " / " + maxX + " - "  + bottom.y(maxX));
        System.out.println("          interpolated " + topLeft.toString() + " - " + bottomRight.toString());

        System.out.println("angle : " + Math.toDegrees(angle) + "°\n");

    }

    private double intersection(Serie vertical, Serie horizontal) {
        double a = horizontal.getA();
        return (a*(vertical.getValueAtOrigin() - horizontal.getValueAtOrigin()))/((a*a) + 1D);
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

    private int firstBlackCoordRight(int y) {
        int cpt = 0;
        while (cpt < 1000000) {
            int c = minX + cpt++ + y * originalWidth;
            if (coords.contains(c)) {
                return c;
            }
        }
        return -1;
    }

    private int firstBlackCoordLeft(int y) {
        int cpt = 0;
        while (cpt < 1000000) {
            int c = maxX - cpt++ + y * originalWidth;
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
        return x > left.x(y) && x < right.x(y) && y > top.y(x) && y < bottom.y(x);
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

    public Serie.Point getTopLeft() {
        return topLeft;
    }

    public Serie.Point getBottomRight() {
        return bottomRight;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
}
