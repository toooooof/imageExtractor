package net.toooooof;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class Serie {

    public static class Point {

        private int x;
        private int y;

        public Point(int x, int y) {
            this.x = x;
            this.y = y;
        }

        public int getX() {
            return x;
        }

        public int getY() {
            return y;
        }
    }

    private List<Point> points;
    private int width;
    private int xAverage;
    private int yAverage;

    private double variance;
    private double covariance;

    public Serie(List<Integer> coords, int width) {
        points = coords.stream().map(c -> new Point(c % width, (int)Math.floor(c / width))).collect(Collectors.toList());
        this.width = width;

        xAverage = points.stream().mapToInt(Point::getX).sum() / points.size();
        yAverage = points.stream().mapToInt(Point::getY).sum() / points.size();

        variance = points.stream().mapToInt(p -> (p.getX() - xAverage) * (p.getX() - xAverage)).average().orElse(-1);
        covariance = points.stream().mapToInt(p -> (p.getX() - xAverage) * (p.getY() - yAverage)).average().orElse(-1);

    }

    public int affine(int x) {
        return (int) (((x - xAverage) * covariance / variance) + yAverage);
    }

}
