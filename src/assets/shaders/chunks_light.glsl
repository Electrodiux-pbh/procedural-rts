#type vertex
#version 330 core

layout(location=0)in vec3 aPos;
layout(location=1)in vec2 aTextCoords;
layout(location=2)in int aAttributes; 

uniform mat4 projection;
uniform mat4 view;

uniform vec4 fogColor;
uniform float fogDistance;
const float gradient = 4;

out vec2 texCoords;
out float visibility;
out vec4 transitionFogColor;

out float lightBrightness;
out vec3 lightColor;

void getLightColor() {
	int r = (aAttributes & 0x00F00000) >> 20;
	int g = (aAttributes & 0x000F0000) >> 16;
	int b = (aAttributes & 0x0000F000) >> 12;

	lightColor = vec3(r / 15.0, g / 15.0, b / 15.0);
}

void getBrightness() {
	int skyLight = (aAttributes & 0xF0000000) >> 28;
	int blockLight = (aAttributes & 0x0F000000) >> 24;
	lightBrightness = clamp(max(blockLight, skyLight) / 15.0, 0.0, 1.0);
}

void main()
{
	vec4 positionRelativeToCamera = view * vec4(aPos, 1);
	gl_Position=projection*positionRelativeToCamera;

	texCoords=aTextCoords;
	getBrightness();
	getLightColor();

	float distance = length(positionRelativeToCamera.xyz);
	visibility = clamp(exp(-pow((distance + 10) / fogDistance, fogDistance / gradient)), 0, 1);
}

#type fragment
#version 330 core

in vec2 texCoords;
in float visibility;
uniform vec4 skyColor;

in float lightBrightness;
in vec3 lightColor;

uniform sampler2D textureSampler;

out vec4 color;

void main()
{
	vec4 textureColor = texture(textureSampler, texCoords);
	if(textureColor.a < 0.5) {
		discard;
	}

	color = textureColor * mix(0.1, 1, lightBrightness) * vec4(mix(vec3(1.0, 1.0, 1.0), lightColor, lightBrightness), 1.0);

	color = mix(skyColor, color, visibility);
}