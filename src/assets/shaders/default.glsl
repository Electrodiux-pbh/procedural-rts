#type vertex
#version 330 core

layout(location=0)in vec3 aPos;
layout(location=1)in vec2 aTextCoords;

uniform mat4 uProjection;
uniform mat4 uView;
uniform mat4 transformMatrix;

out vec2 fTextCoords;

void main()
{
	fTextCoords=aTextCoords;
	gl_Position=uProjection*uView*transformMatrix*vec4(aPos,1.);
}

#type fragment
#version 330 core

in vec2 fTextCoords;

uniform sampler2D uSampler;

out vec4 color;

void main()
{
	color=texture(uSampler,fTextCoords);
}