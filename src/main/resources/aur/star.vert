#version 330

in vec2 a_position;
in vec2 a_centerpos;
in vec4 a_color;
uniform mat4 u_projTrans;
flat out vec2 f_centerpos;
flat out vec4 f_color;
out vec2 v_origincoords;

void main()
{
    f_centerpos = a_centerpos;
    f_color = a_color;
    v_origincoords = a_position;
    gl_Position = u_projTrans*vec4(a_position, 0.0, 1.0);
}
