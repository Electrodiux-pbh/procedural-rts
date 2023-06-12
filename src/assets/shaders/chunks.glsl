#type vertex
#version 330 core

layout(location=0)in vec3 aPos;
layout(location=1)in vec2 aTextCoords;

uniform mat4 projection;
uniform mat4 view;

uniform vec4 fogColor;
uniform vec4 skyColor;

out vec2 texCoords;
out float visibility;

out vec4 transitionFogColor;

uniform float fogDistance;
const float gradient = 4;

void main()
{
	vec4 positionRelativeToCamera = view * vec4(aPos, 1);
	gl_Position=projection*positionRelativeToCamera;

	texCoords=aTextCoords;

	float distance = length(positionRelativeToCamera.xyz);
	visibility = clamp(exp(-pow((distance + 10) / fogDistance, fogDistance / gradient)), 0, 1);

	transitionFogColor = mix(skyColor, fogColor, clamp(exp(-pow((distance - 10) / fogDistance, fogDistance / gradient)), 0, 1));
}

#type fragment
#version 330 core

in vec2 texCoords;
in float visibility;

in vec4 transitionFogColor;

uniform sampler2D textureSampler;

out vec4 color;

void main()
{
	vec4 textureColor = texture(textureSampler, texCoords);
	if(textureColor.a < 0.5) {
		discard;
	}

	color = mix(transitionFogColor, textureColor, visibility);
}