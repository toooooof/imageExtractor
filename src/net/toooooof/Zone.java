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

    private int originalWidth;


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

    private void findBoundingRectangle() {
        int l = (int) Math.floor(((double) (maxX - minX)) * Extractor.search_boundaries_percentage / 2d);
        int start = minX + l;
        int end = maxX - l;

        List<Integer> bottom = new ArrayList<>();
        List<Integer> top = new ArrayList<>();

        for (int x = start ; x < end ; x++) {
            bottom.add(firstBlackCoordUp(x));
            top.add(firstBlackCoordDown(x));
        }

        Serie sBottom = new Serie(bottom, originalWidth);
        System.out.println("     ===> " +minX + " - "  + sBottom.affine(minX) + " / " + maxX + " - "  + sBottom.affine(maxX));

        System.out.println(Math.toDegrees(Math.atan((double) Math.abs(sBottom.affine(maxX) - maxY) / (double) getWidth())) + "°");

        System.out.println(bottom.size());
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
}
