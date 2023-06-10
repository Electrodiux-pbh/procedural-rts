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

	private float aspectRatio;
	private float zFar;
	private float zNear;
	private float fov;

	private Color bg;

	public Camera() {
		this(16f / 9f);
	}

	public Camera(float aspectRatio) {
		this(FOV, aspectRatio, NEAR_PLANE, FAR_PLANE);
	}

	public Camera(float fov, float aspectRatio, float nearPlane, float farPlane) {
		this(new Vector3(0, 0, 0), new Vector3(0, 0, 0), fov, aspectRatio, nearPlane, farPlane);
	}

	public Camera(Vector3 position, Vector3 rotation, float fov, float aspectRatio, float nearPlane, float farPlane) {
		this.position = position;
		this.rotation = rotation;

		this.fov = fov;
		this.aspectRatio = aspectRatio;
		this.zNear = nearPlane;
		this.zFar = farPlane;

		this.bg = new Color(Color.WHITE);

		this.viewMatrix = new Matrix4f();
		this.projectionMatrix = new Matrix4f();
		makeProjection();
	}

	public void makeProjection() {
		projectionMatrix.identity();
		projectionMatrix.perspective(fov, aspectRatio, zNear, zFar);
	}

	public Matrix4f getViewMatrix() {
		viewMatrix.identity();

		viewMatrix.rotate(rotation.x(), 1, 0, 0);
		viewMatrix.rotate(rotation.y(), 0, 1, 0);
		viewMatrix.rotate(rotation.z(), 0, 0, 1);

		viewMatrix.translate(-position.x(), -position.y(), -position.z());
		return this.viewMatrix;
	}

	public void clearColor() {
		GL11.glClearColor(bg.r(), bg.g(), bg.b(), bg.a());
	}

	public void setProjectionsToShader(Shader shader) {
		shader.setMatrix4f("projection", getProjectionMatrix());
		shader.setMatrix4f("view", getViewMatrix());
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

	public float getAspectRatio() {
		return aspectRatio;
	}

	public void setAspectRatio(float width, float height) {
		float ratio = width / height;
		if (this.aspectRatio != ratio) {
			this.aspectRatio = ratio;
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