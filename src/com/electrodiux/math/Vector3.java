package com.electrodiux.math;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Collection;

import org.joml.Vector3f;

public class Vector3 implements Serializable {

    public static final Vector3 ZERO = new Vector3(0, 0, 0);
    public static final Vector3 ONE = new Vector3(1, 1, 1);

    public static final Vector3 BACK = new Vector3(0, 0, -1);
    public static final Vector3 FORWARD = new Vector3(0, 0, 1);

    public static final Vector3 UP = new Vector3(0, 1, 0);
    public static final Vector3 DOWN = new Vector3(0, -1, 0);

    public static final Vector3 LEFT = new Vector3(-1, 0, 0);
    public static final Vector3 RIGHT = new Vector3(1, 0, 0);

    public static final Vector3 POSITIVE_INFINITY = new Vector3(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY,
            Float.POSITIVE_INFINITY);
    public static final Vector3 NEGATIVE_INFINITY = new Vector3(Float.NEGATIVE_INFINITY, Float.NEGATIVE_INFINITY,
            Float.NEGATIVE_INFINITY);

    public float x = 0.0f;
    public float y = 0.0f;
    public float z = 0.0f;

    public Vector3() {
    }

    public static Vector3 zeroVector() {
        return new Vector3(0, 0, 0);
    }

    public Vector3(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Vector3(Vector3 v) {
        if (v != null) {
            this.x = v.x;
            this.y = v.y;
            this.z = v.z;
        }
    }

    public Vector3(Vector2 v) {
        if (v != null) {
            this.x = v.x;
            this.y = v.y;
        }
    }

    public float x() {
        return x;
    }

    public float y() {
        return y;
    }

    public float z() {
        return z;
    }

    public void x(float x) {
        this.x = x;
    }

    public void y(float y) {
        this.y = y;
    }

    public void z(float z) {
        this.z = z;
    }

    public Vector3 set(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
        return this;
    }

    public Vector3 set(Vector3 v) {
        if (v != null) {
            this.x = v.x;
            this.y = v.y;
            this.z = v.z;
        }
        return this;
    }

    public Vector3 setScaled(Vector3 v, float scalar) {
        if (v != null) {
            this.x = v.x * scalar;
            this.y = v.y * scalar;
            this.z = v.z * scalar;
        }
        return this;
    }

    public Vector3 set(Vector2 v) {
        if (v != null) {
            this.x = v.x;
            this.y = v.y;
        }
        return this;
    }

    public float magnitude() {
        return Vector3.magnitude(this);
    }

    public static float magnitude(Vector3 v) {
        return (float) Math.sqrt(v.x * v.x + v.y * v.y + v.z * v.z);
    }

    public void magnitude(float magnitude) {
        normalize();
        x *= magnitude;
        y *= magnitude;
        z *= magnitude;
    }

    public float sqrMagnitude() {
        return Vector3.sqrMagnitude(this);
    }

    public static float sqrMagnitude(Vector3 v) {
        return v.x * v.x + v.y * v.y + v.z * v.z;
    }

    public static float distance(Vector3 v1, Vector3 v2) {
        float dx = v2.x - v1.x;
        float dy = v2.y - v1.y;
        float dz = v2.z - v1.z;
        return (float) Math.sqrt(dx * dx + dy * dy + dz * dz);
    }

    public static float sqrDistance(Vector3 v1, Vector3 v2) {
        float dx = v2.x - v1.x;
        float dy = v2.y - v1.y;
        float dz = v2.z - v1.z;
        return dx * dx + dy * dy + dz * dz;
    }

    public Vector3 inverseSign() {
        x = -x;
        y = -y;
        z = -z;
        return this;
    }

    public Vector3 add(float x1, float y2, float z3) {
        x += x1;
        y += y2;
        z += z3;
        return this;
    }

    public Vector3 add(Vector3 other) {
        x += other.x;
        y += other.y;
        z += other.z;
        return this;
    }

    public Vector3 addScaled(Vector3 other, float scale) {
        x += other.x * scale;
        y += other.y * scale;
        z += other.z * scale;
        return this;
    }

    public Vector3 getAdded(Vector3 other) {
        return Vector3.add(this, other);
    }

    public static Vector3 add(Vector3 v1, Vector3 v2) {
        return new Vector3(v1.x + v2.x, v1.y + v2.y, v1.z + v2.z);
    }

    public Vector3 subtract(float x1, float y2, float z3) {
        x -= x1;
        y -= y2;
        z -= z3;
        return this;
    }

    public Vector3 subtract(Vector3 other) {
        x -= other.x;
        y -= other.y;
        z -= other.z;
        return this;
    }

    public Vector3 getSubstract(Vector3 other) {
        return Vector3.subtract(this, other);
    }

    public static Vector3 subtract(Vector3 v1, Vector3 v2) {
        return new Vector3(v1.x - v2.x, v1.y - v2.y, v1.z - v2.z);
    }

    public float dot(Vector3 other) {
        return Vector3.dot(this, other);
    }

    public static float dot(Vector3 v1, Vector3 v2) {
        return v1.x * v2.x + v1.y * v2.y + v1.z * v2.z;
    }

    public Vector3 mul(float scalar) {
        x *= scalar;
        y *= scalar;
        z *= scalar;
        return this;
    }

    public Vector3 getMul(float scalar) {
        return Vector3.mul(this, scalar);
    }

    public static Vector3 mul(Vector3 v, float scalar) {
        return new Vector3(v.x * scalar, v.y * scalar, v.z * scalar);
    }

    public Vector3 div(float div) {
        x /= div;
        y /= div;
        z /= div;
        return this;
    }

    public Vector3 getDiv(float div) {
        return Vector3.mul(this, div);
    }

    public static Vector3 div(Vector3 v, float div) {
        return new Vector3(v.x / div, v.y / div, v.z / div);
    }

    public Vector3 cross(Vector3 other) {
        this.x = y * other.z - z * other.y;
        this.y = z * other.x - x * other.z;
        this.z = x * other.y - y * other.x;
        return this;
    }

    public Vector3 getCross(Vector3 other) {
        return Vector3.cross(this, other);
    }

    public static Vector3 cross(Vector3 v1, Vector3 v2) {
        float newX = v1.y * v2.z - v1.z * v2.y;
        float newY = v1.z * v2.x - v1.x * v2.z;
        float newZ = v1.x * v2.y - v1.y * v2.x;
        return new Vector3(newX, newY, newZ);
    }

    public Vector3 normalize() {
        float magnitude = magnitude();
        if (magnitude == 0) {
            zero();
            return this;
        }
        mul(1.0f / magnitude);
        return this;
    }

    public Vector3 getNormalized() {
        return Vector3.normalize(this);
    }

    public static Vector3 normalize(Vector3 v) {
        float magnitude = v.magnitude();
        if (magnitude == 0) {
            return new Vector3(0, 0, 0);
        }
        return Vector3.mul(v, 1.0f / magnitude);
    }

    public static Vector3 max(Vector3 a, Vector3 b) {
        float x = a.x > b.x ? a.x : b.x;
        float y = a.y > b.y ? a.y : b.y;
        float z = a.z > b.z ? a.z : b.z;
        return new Vector3(x, y, z);
    }

    public static Vector3 min(Vector3 a, Vector3 b) {
        float x = a.x < b.x ? a.x : b.x;
        float y = a.y < b.y ? a.y : b.y;
        float z = a.z < b.z ? a.z : b.z;
        return new Vector3(x, y, z);
    }

    public static Vector3 addAll(Collection<Vector3> vectors) {
        if (vectors == null)
            return new Vector3(0, 0, 0);

        Vector3 result = new Vector3();

        for (Vector3 v : vectors) {
            if (v != null)
                result.add(v);
        }

        return result;
    }

    public static Vector3 lerp(Vector3 a, Vector3 b, float t) {
        if (t < 0) {
            return new Vector3(a);
        } else if (t > 1) {
            return new Vector3(b);
        }

        float x = a.x + (b.x - a.x) * t;
        float y = a.y + (b.y - a.y) * t;
        float z = a.z + (b.z - a.z) * t;

        return new Vector3(x, y, z);
    }

    public void zero() {
        this.x = 0;
        this.y = 0;
        this.z = 0;
    }

    public Vector3 clone() {
        return new Vector3(this.x, this.y, this.z);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + Float.floatToIntBits(x);
        result = prime * result + Float.floatToIntBits(y);
        result = prime * result + Float.floatToIntBits(z);
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj instanceof Vector3 vector)
            return equals(vector);
        return false;
    }

    public boolean equals(Vector3 other) {
        if (other == null)
            return false;
        return other.x == this.x && other.y == this.y && other.z == this.z;
    }

    public boolean equals(float x, float y, float z) {
        return x == this.x && y == this.y && z == this.z;
    }

    @Override
    public String toString() {
        return "(" + x + ", " + y + ", " + z + ")";
    }

    // #region Serialization

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeFloat(x);
        out.writeFloat(y);
        out.writeFloat(z);
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        x = in.readFloat();
        y = in.readFloat();
        z = in.readFloat();
    }

    // #endregion

    public static Vector3f toOGMLVector(Vector3 v) {
        return new Vector3f(v.x, v.y, v.z);
    }

}
