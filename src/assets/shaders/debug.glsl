#type vertex
#version 330 core

layout(location=0)in vec3 aPos;
layout(location=1)in vec3 aColor;

uniform mat4 projection;
uniform mat4 view;

out vec3 fColor;

void main()
{
    fColor=aColor;
    gl_Position=projection*view*vec4(aPos,1.);
}

#type fragment
#version 330 core

in vec3 fColor;

out vec4 color;

void main()
{
    color=vec4(fColor,1);
}