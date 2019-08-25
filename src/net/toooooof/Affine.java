package net.toooooof;

public class Affine {

    protected double a;
    protected double b;

    public Affine() {
    }

    public Affine(double a, double b) {
        this.a = a;
        this.b = b;
    }

    public double y(double x) {
        return a * x + b;
    }

    public double x(double y) {
        return (y - b) / a;
    }

    public static Affine perpendicalar(double a, double b) {

        if (a == 0D) {
            throw new IllegalArgumentException("a cannot be null");
        }

        return new Affine(-1/a, b);
    }

    public double getA() {
        return a;
    }
}
