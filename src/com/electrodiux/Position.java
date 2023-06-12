package com.electrodiux;

public class Position {

    private float x, y, z;

    public Position() {
        this(0, 0, 0);
    }

    public Position(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public void set(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public void add(float x, float y, float z) {
        this.x += x;
        this.y += y;
        this.z += z;
    }

    public float x() {
        return x;
    }

    public int getBlockX() {
        return (int) x;
    }

    public void setX(float x) {
        this.x = x;
    }

    public float y() {
        return y;
    }

    public int getBlockY() {
        return (int) y;
    }

    public void setY(float y) {
        this.y = y;
    }

    public float z() {
        return z;
    }

    public int getBlockZ() {
        return (int) z;
    }

    public void setZ(float z) {
        this.z = z;
    }

}
