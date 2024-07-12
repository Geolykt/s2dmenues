#version 330

in vec2 a_position;
in vec2 a_centerpos;
uniform mat4 u_projTrans;
out vec2 v_originoffset;

void main()
{
    v_originoffset = a_position-a_centerpos;
    gl_Position = u_projTrans*vec4(a_position, 0.0, 1.0);
}
