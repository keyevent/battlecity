package dingziyuan.utils;

public class Vector3 {
    private double x, y, z;

    public Vector3(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Vector3(double radius) {
        this.x = Math.sin(radius);
        this.y = Math.cos(radius);
        this.z = 0;

//        return new Vector3(Math.cos(radius),Math.sin(radius),0);
    }

    public Vector3 add(Vector3 b) {
        return new Vector3(this.x + b.x, this.y + b.y, this.z + b.z);
    }

    public Vector3 sub(Vector3 b) {
        return new Vector3(this.x - b.x, this.y - b.y, this.z - b.z);
    }

    public double dot(Vector3 b) {
        return this.x * b.x + this.y * b.y + this.z * b.z;
    }

    public Vector3 cross(Vector3 b) {
        return new Vector3(
                this.y * b.z - this.z * b.y,
                -this.x * b.z + this.z * b.x,
                this.x * b.y - this.y * b.x);
    }

    public Vector3 mul(double factor) {
        return new Vector3(this.x * factor, this.y * factor, this.z * factor);
    }

    public double norm() {
        return Math.sqrt(this.x * this.x + this.y * this.y + this.z * this.z);
    }

    @Override
    public String toString() {
        return "Vector3{" +
                "x=" + x +
                ", y=" + y +
                ", z=" + z +
                '}';
    }

    public Vector3 normlize() {
        return new Vector3(this.x / this.norm(), this.y / this.norm(), this.z / this.norm());
    }

    public Boolean isOnMyRight(Vector3 b) {
        return this.cross(b).z > 0 ? false : true;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getZ() {
        return z;
    }
}
