#type vertex
#version 330 core

layout(location=0)in vec3 aPos;
layout(location=1)in vec2 aTextCoords;

uniform mat4 transform;
uniform mat4 projection;
uniform mat4 view;

out vec2 texCoords;

void main()
{
	vec4 positionRelativeToCamera = view * transform * vec4(aPos, 1);
	gl_Position=projection*positionRelativeToCamera;

	texCoords=aTextCoords;
}

#type fragment
#version 330 core

in vec2 texCoords;

uniform sampler2D textureSampler;

out vec4 color;

void main()
{
	vec4 textureColor = texture(textureSampler, texCoords);
	if(textureColor.a < 0.5) {
		discard;
	}
	color = textureColor;
}