package com.electrodiux.math;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Collection;

import org.joml.Vector2f;

public class Vector2 implements Serializable {

    public static final Vector2 ZERO = new Vector2(0, 0);
    public static final Vector2 ONE = new Vector2(1, 1);

    public static final Vector2 UP = new Vector2(0, 1);
    public static final Vector2 DOWN = new Vector2(0, -1);

    public static final Vector2 LEFT = new Vector2(-1, 0);
    public static final Vector2 RIGHT = new Vector2(1, 0);

    public static final Vector2 POSITIVE_INFINITY = new Vector2(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY);
    public static final Vector2 NEGATIVE_INFINITY = new Vector2(Float.NEGATIVE_INFINITY, Float.NEGATIVE_INFINITY);

    protected float x;
    protected float y;

    public Vector2() {
        this.x = 0.0f;
        this.y = 0.0f;
    }

    public Vector2(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public Vector2(Vector2 v) {
        this.x = v.x;
        this.y = v.y;
    }

    public float x() {
        return x;
    }

    public float y() {
        return y;
    }

    public void x(float x) {
        this.x = x;
    }

    public void y(float y) {
        this.y = y;
    }

    public void set(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public void set(Vector2 v) {
        if (v != null) {
            this.x = v.x;
            this.y = v.y;
        }
    }

    public float magnitude() {
        return (float) Math.sqrt(x * x + y * y);
    }

    public float sqrMagnitude() {
        return x * x + y * y;
    }

    public static float distance(Vector2 v1, Vector2 v2) {
        float dx = v2.x - v1.x;
        float dy = v2.y - v1.y;
        return (float) Math.sqrt(dx * dx + dy * dy);
    }

    public static float sqrDistance(Vector2 v1, Vector2 v2) {
        float dx = v2.x - v1.x;
        float dy = v2.y - v1.y;
        return dx * dx + dy * dy;
    }

    public Vector2 add(Vector2 other) {
        x += other.x;
        y += other.y;
        return this;
    }

    public Vector2 add(float x1, float y2) {
        x += x1;
        y += y2;
        return this;
    }

    public Vector2 getAdded(Vector2 other) {
        return new Vector2(x + other.x, y + other.y);
    }

    public Vector2 subtract(float x1, float y2) {
        x -= x1;
        y -= y2;
        return this;
    }

    public Vector2 subtract(Vector2 other) {
        x -= other.x;
        y -= other.y;
        return this;
    }

    public Vector2 getSubstract(Vector2 other) {
        return new Vector2(x - other.x, y - other.y);
    }

    public float dot(Vector2 other) {
        return x * other.x + y * other.y;
    }

    public Vector2 scale(float scalar) {
        x *= scalar;
        y *= scalar;
        return this;
    }

    public Vector2 getScaled(float scalar) {
        return new Vector2(x * scalar, y * scalar);
    }

    public Vector2 normalize() {
        float magnitude = magnitude();
        if (magnitude == 0) {
            x = 0;
            y = 0;
            return this;
        }
        scale(1 / magnitude);
        return this;
    }

    public void zero() {
        this.x = 0;
        this.y = 0;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + Float.floatToIntBits(x);
        result = prime * result + Float.floatToIntBits(y);
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        return equals((Vector2) obj);
    }

    public boolean equals(Vector2 other) {
        if (other == null)
            return false;
        return other.x == this.x && other.y == this.y;
    }

    @Override
    public String toString() {
        return "(" + x + ", " + y + ")";
    }

    public static Vector2 addAll(Collection<Vector2> vectors) {
        if (vectors == null)
            return new Vector2(0, 0);

        Vector2 result = new Vector2();

        for (Vector2 v : vectors) {
            if (v != null)
                result.add(v);
        }

        return result;
    }

    public static Vector2 lerp(Vector2 a, Vector2 b, float t) {
        if (t < 0) {
            return new Vector2(a);
        } else if (t > 1) {
            return new Vector2(b);
        }

        float x = a.x + (b.x - a.x) * t;
        float y = a.y + (b.y - a.y) * t;

        return new Vector2(x, y);
    }

    public static Vector2f toOGMLVector(Vector2 v) {
        return new Vector2f(v.x, v.y);
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeFloat(x);
        out.writeFloat(y);
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        x = in.readFloat();
        y = in.readFloat();
    }

}
