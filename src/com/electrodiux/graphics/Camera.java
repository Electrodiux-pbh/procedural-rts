package com.electrodiux.graphics;

import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;

import com.electrodiux.math.Vector3;

public class Camera {

	public static final float FOV = 70;
	public static final float MIN_FOV = 10;
	public static final float MAX_FOV = 150;
	public static final float NEAR_PLANE = 0.1F;
	public static final float FAR_PLANE = 100;

	private transient Matrix4f projectionMatrix, viewMatrix;
	private Vector3 position;
	private Vector3 rotation;

	private float width, height;

	private float zFar;
	private float zNear;
	private float fov;

	private Color bg;

	public Camera(float width, float height) {
		this(FOV, width, height, NEAR_PLANE, FAR_PLANE);
	}

	public Camera(float fov, float width, float height, float nearPlane, float farPlane) {
		this(new Vector3(0, 0, 0), new Vector3(0, 0, 0), fov, width, height, nearPlane, farPlane);
	}

	public Camera(Vector3 position, Vector3 rotation, float fov, float width, float height, float nearPlane,
			float farPlane) {
		this.position = position;
		this.rotation = rotation;

		this.fov = fov;
		this.zNear = nearPlane;
		this.zFar = farPlane;
		this.width = width;
		this.height = height;

		this.bg = new Color(Color.WHITE);

		this.viewMatrix = new Matrix4f();
		this.projectionMatrix = new Matrix4f();
		makeProjection();
	}

	public void makeProjection() {
		projectionMatrix.identity();
		projectionMatrix.perspective(fov, width / height, zNear, zFar);
	}

	public Matrix4f getViewMatrix() {
		viewMatrix.identity();

		viewMatrix.rotate(rotation.x(), 1, 0, 0);
		viewMatrix.rotate(rotation.y(), 0, 1, 0);
		viewMatrix.rotate(rotation.z(), 0, 0, 1);

		viewMatrix.translate(-position.x(), -position.y(), -position.z());
		return this.viewMatrix;
	}

	public Matrix4f getProjectionViewMatrix() {
		return getViewMatrix().mulLocal(getProjectionMatrix());
	}

	public void clearColor() {
		GL11.glClearColor(bg.r(), bg.g(), bg.b(), bg.a());
	}

	public Matrix4f getProjectionMatrix() {
		return projectionMatrix;
	}

	public Vector3 position() {
		return position;
	}

	public Vector3 rotation() {
		return rotation;
	}

	public void setBackgroundColor(Color bg) {
		this.bg = bg;
	}

	public Color getBackgroundColor() {
		return bg;
	}

	public float getWidth() {
		return width;
	}

	public float getHeight() {
		return height;
	}

	public void setDimensions(int width, int height) {
		if (this.width != width || this.height != height) {
			this.width = width;
			this.height = height;
			makeProjection();
		}
	}

	public float getzFar() {
		return zFar;
	}

	public void setzFar(float farPlane) {
		if (this.zFar != farPlane) {
			this.zFar = farPlane;
			makeProjection();
		}
	}

	public float getzNear() {
		return zNear;
	}

	public void setzNear(float nearPlane) {
		if (this.zNear != nearPlane) {
			this.zNear = nearPlane;
			makeProjection();
		}
	}

	public float getFov() {
		return fov;
	}

	public void setFov(float fov) {
		if (this.fov != fov) {
			this.fov = fov;
			makeProjection();
		}
	}

}