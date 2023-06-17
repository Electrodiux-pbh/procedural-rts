package com.electrodiux.graphics.textures;

import org.lwjgl.opengl.GL11;

public class Texture {

	protected int textureId;

	public Texture(int textureId) {
		this.textureId = textureId;
	}

	public void bind() {
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureId);
	}

	public void unbind() {
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
	}

	public void delete() {
		GL11.glDeleteTextures(textureId);
	}

	public int getTextureId() {
		return textureId;
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
