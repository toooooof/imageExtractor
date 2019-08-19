package net.toooooof;

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
    private int xAverage;
    private int yAverage;

    private double variance;
    private double covariance;

    private double a;

    private boolean descending;

    public Serie(List<Integer> coords, int width) {
        points = coords.stream().map(c -> new Point(c % width, (int)Math.floor(c / width))).collect(Collectors.toList());

        xAverage = points.stream().mapToInt(Point::getX).sum() / points.size();
        yAverage = points.stream().mapToInt(Point::getY).sum() / points.size();

        variance = points.stream().mapToInt(p -> (p.getX() - xAverage) * (p.getX() - xAverage)).average().orElse(-1);
        covariance = points.stream().mapToInt(p -> (p.getX() - xAverage) * (p.getY() - yAverage)).average().orElse(-1);

        a = covariance / variance;

        computeDirections();
    }

    public Serie(List<Integer> coords, int width, int a) {
        points = coords.stream().map(c -> new Point(c % width, (int)Math.floor(c / width))).collect(Collectors.toList());

        xAverage = points.stream().mapToInt(Point::getX).sum() / points.size();
        yAverage = points.stream().mapToInt(Point::getY).sum() / points.size();

        this.a = a;

        computeDirections();
    }

    private void computeDirections() {
        int trendHorizontal = 0;
        for (int i = 1 ; i < points.size() ; i++) {
            if (points.get(i).getY() > points.get(i-1).getY()) {
                trendHorizontal++;
            } else if (points.get(i).getY() < points.get(i-1).getY()) {
                trendHorizontal--;
            }
        }
        this.descending = trendHorizontal > 0;
    }

    /**
     * Can also be written as y = ax + b
     * y = x*cov/var + yAv - xAv*cov/var
     *
     *
     * @param x
     * @return
     */
    public int affine(int x) {
        return (int) (((x - xAverage) * a) + yAverage);
    }

    public int invAffine(int y) {
        return (int) (((y - yAverage) / a) + xAverage);
    }

    public boolean isDescending() {
        return descending;
    }

    public int getValueAtOrigin() {
        return yAverage - (int) (xAverage * a);
    }

    public void setAFromAngle(double angle) {
        this.a = Math.sin(angle);
    }
}
