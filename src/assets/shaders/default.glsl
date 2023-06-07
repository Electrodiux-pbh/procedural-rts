#type vertex
#version 330 core

layout(location=0)in vec3 aPos;
layout(location=1)in vec2 aTextCoords;

uniform mat4 uProjection;
uniform mat4 uView;

out vec2 fTextCoords;

void main()
{
	fTextCoords=aTextCoords;
	gl_Position=uProjection*uView*vec4(aPos,1.);
}

#type fragment
#version 330 core

in vec2 fTextCoords;

uniform sampler2D uSampler;

out vec4 color;

void main()
{
	vec4 textureColor = texture(uSampler,fTextCoords);
	if(textureColor.a < 0.5) {
		discard;
	}
	color = textureColor;
}