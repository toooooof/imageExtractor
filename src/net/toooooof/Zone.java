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
        int scanWidth = (int) Math.floor(((double) (maxX - minX)) * Extractor.search_boundaries_percentage / 2d);
        int startW = minX + scanWidth;
        int endW = maxX - scanWidth;
//        int scanHeight = (int) Math.floor(((double) (maxY - minY)) * Extractor.search_boundaries_percentage / 2d);
//        int startH = minY + scanHeight;
//        int endH = maxY - scanHeight;

        List<Integer> bottom = new ArrayList<>();
        List<Integer> top = new ArrayList<>();
//        List<Integer> left = new ArrayList<>();
//        List<Integer> right = new ArrayList<>();

        for (int x = startW ; x < endW ; x++) {
            bottom.add(firstBlackCoordUp(x));
            top.add(firstBlackCoordDown(x));
        }

        /*for (int y = startH ; y < endH ; y++) {
            left.add(firstBlackCoordRight(y));
            right.add(firstBlackCoordLeft(y));
        }*/

        Serie sBottom = new Serie(bottom, originalWidth);
        boolean descendingBottom = sBottom.isDescending();
        double oppositeBottom = descendingBottom ? (double) Math.abs(sBottom.affine(minX) - maxY) : (double) Math.abs(sBottom.affine(maxX) - maxY);
        int coordBottomX = sBottom.invAffine(maxY);
        double adjacentBottom = descendingBottom ? coordBottomX - minX : maxX - coordBottomX;
        double angleBottom = Math.atan(oppositeBottom / adjacentBottom);

        Serie sTop = new Serie(top, originalWidth);
        boolean descendingTop = sTop.isDescending();
        double oppositeTop = descendingTop ? (double) Math.abs(sTop.affine(maxX) - minY) : (double) Math.abs(sTop.affine(minX) - minY);
        int coordTopX = sTop.invAffine(minY);
        double adjacentTop = descendingTop ? maxX - coordTopX: coordTopX - minX;
        double angleTop = Math.atan(oppositeTop / adjacentTop);

        double angle = (angleBottom + angleTop) / 2;

/*
        Serie sLeft = new Serie(left, originalWidth);
        boolean forwardLeft = sLeft.isForward();
        double oppositeLeft = forwardLeft ? sLeft.invAffine(minY) - minX : sLeft.invAffine(maxY) - minX;
        int coordLeftY = sLeft.affine(minX);
        double adjacentLeft = forwardLeft ? coordLeftY - minY : maxY - coordLeftY;
        double angleLeft = Math.atan(oppositeLeft / adjacentLeft);*/

        System.out.println("     ===> " +minX + " - "  + sBottom.affine(minX) + " / " + maxX + " - "  + sBottom.affine(maxX));

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
/*

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
*/

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
