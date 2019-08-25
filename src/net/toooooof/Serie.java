package net.toooooof;

import java.util.List;
import java.util.stream.Collectors;

public class Serie extends Affine {

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

        public String toString() {
            return "[" + x + "," + y + "]";
        }
    }

    private List<Point> points;
    private int xAverage;
    private int yAverage;

    private double variance;
    private double covariance;

    public Serie(List<Integer> coords, int width) {
        points = coords.stream().map(c -> new Point(c % width, (int)Math.floor(c / width))).collect(Collectors.toList());

        xAverage = points.stream().mapToInt(Point::getX).sum() / points.size();
        yAverage = points.stream().mapToInt(Point::getY).sum() / points.size();

        variance = points.stream().mapToInt(p -> (p.getX() - xAverage) * (p.getX() - xAverage)).average().orElse(-1);
        covariance = points.stream().mapToInt(p -> (p.getX() - xAverage) * (p.getY() - yAverage)).average().orElse(-1);

        this.a = covariance / variance;
        this.b = yAverage - xAverage*a;

    }

    public Serie(List<Integer> coords, int width, double a) {
        points = coords.stream().map(c -> new Point(c % width, (int)Math.floor(c / width))).collect(Collectors.toList());

        xAverage = points.stream().mapToInt(Point::getX).sum() / points.size();
        yAverage = points.stream().mapToInt(Point::getY).sum() / points.size();

        this.a = a;
        this.b = yAverage - xAverage*a;

    }

    /**
     * Can also be written as y = ax + b
     * y = x*cov/var + yAv - xAv*cov/var
     *
     *
     * @param x
     * @return
     */
    @Deprecated
    public int affine(int x) {
        return (int) (((x - xAverage) * a) + yAverage);
    }

    @Deprecated
    public int invAffine(int y) {
        return (int) (((y - yAverage) / a) + xAverage);
    }

    public boolean isDescending() {
        return a > 0;
    }

    public int getValueAtOrigin() {
        return yAverage - (int) (xAverage * a);
    }

    public void setAFromAngle(double angle) {
        this.a = Math.sin(angle);
    }
}
