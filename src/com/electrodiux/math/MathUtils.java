package com.electrodiux.math;

import org.joml.Matrix4f;

public final class MathUtils {

    private MathUtils() {
    }

    public static float clamp(float min, float value, float max) {
        if (value < min)
            return min;
        if (value > max)
            return max;
        return value;
    }

    public static float lerp(float a, float b, float t) {
        return a + (b - a) * t;
    }

    public static Matrix4f transformMatrix(Vector3 position, Vector3 rotation, Vector3 scale, Matrix4f target) {
        target.identity();

        target.translate(position.x(), position.y(), position.z());
        target.rotate(rotation.x(), 1, 0, 0);
        target.rotate(rotation.y(), 0, 1, 0);
        target.rotate(rotation.z(), 0, 0, 1);
        target.scale(scale.x(), scale.y(), scale.z());

        return target;
    }

    public static Matrix4f transformMatrix(Vector3 position, Vector3 rotation, float scale, Matrix4f target) {
        target.identity();

        target.translate(position.x(), position.y(), position.z());
        target.rotate(rotation.x(), 1, 0, 0);
        target.rotate(rotation.y(), 0, 1, 0);
        target.rotate(rotation.z(), 0, 0, 1);
        target.scale(scale, scale, scale);

        return target;
    }

    public static float barryCentric(Vector3 p1, Vector3 p2, Vector3 p3, Vector2 pos) {
        float det = (p2.z - p3.z) * (p1.x - p3.x) + (p3.x - p2.x) * (p1.z - p3.z);

        float l1 = ((p2.z - p3.z) * (pos.x - p3.x) + (p3.x - p2.x) * (pos.y - p3.z)) / det;
        float l2 = ((p3.z - p1.z) * (pos.x - p3.x) + (p1.x - p3.x) * (pos.y - p3.z)) / det;
        float l3 = 1.0f - l1 - l2;

        return l1 * p1.y + l2 * p2.y + l3 * p3.y;
    }

    public static Vector3 barryCentric(Vector2 p1, Vector2 p2, Vector2 p3, Vector2 pos, Vector3 v1, Vector3 v2,
            Vector3 v3) {
        float det = (p2.y() - p3.y()) * (p1.x() - p3.x()) + (p3.x() - p2.x()) * (p1.y() - p3.y());

        float l1 = ((p2.y() - p3.y()) * (pos.x() - p3.x()) + (p3.x() - p2.x()) * (pos.y() - p3.y())) / det;
        float l2 = ((p3.y() - p1.y()) * (pos.x() - p3.x()) + (p1.x() - p3.x()) * (pos.y() - p3.y())) / det;
        float l3 = 1.0f - l1 - l2;

        return v1.getMul(l1).add(v2.getMul(l2)).add(v3.getMul(l3));
    }

}
