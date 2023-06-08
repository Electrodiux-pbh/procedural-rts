package com.electrodiux.graphics.textures;

import org.lwjgl.opengl.GL11;

public class Texture {

	protected int textureId;
	private int width, height;

	public Texture(int textureId) {
		this.textureId = textureId;
	}

	public Texture(int textureId, int width, int height) {
		this.textureId = textureId;
		this.width = width;
		this.height = height;
	}

	public void bind() {
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureId);
	}

	public void unbind() {
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
	}

	public int getTextureId() {
		return textureId;
	}

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null)
			return false;
		if (!(obj instanceof Texture))
			return false;

		Texture objTex = (Texture) obj;
		return objTex.textureId == this.textureId;
	}

}
