#type vertex
#version 330 core

layout(location=0)in vec3 aPos;
layout(location=1)in vec2 aTextCoords;

uniform mat4 projection;
uniform mat4 view;

out vec2 texCoords;
out float visibility;

uniform float fogDistance = 12 * 16;
const float gradient = 2.7;

void main()
{
	vec4 positionRelativeToCamera = view * vec4(aPos, 1);
	gl_Position=projection*positionRelativeToCamera;

	texCoords=aTextCoords;

	float distance = length(positionRelativeToCamera.xyz);
	visibility = clamp(exp(-pow(distance / fogDistance, fogDistance / gradient)), 0, 1);
}

#type fragment
#version 330 core

in vec2 texCoords;
in float visibility;

uniform sampler2D textureSampler;
uniform vec4 skyColor;

out vec4 color;

void main()
{
	vec4 textureColor = texture(textureSampler, texCoords);
	if(textureColor.a < 0.5) {
		discard;
	}
	color = mix(skyColor, textureColor, visibility);
}