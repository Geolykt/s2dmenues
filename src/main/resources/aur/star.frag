#version 330

uniform mat4 u_projTrans;
flat in vec2 f_centerpos;
flat in vec4 f_color;
in vec2 v_origincoords;
layout(location = 0) out vec4 frag_color;

void main()
{
    float d = distance(v_origincoords, f_centerpos);

    if (d < 0.005) {
        frag_color = vec4(1.0, 1.0, 1.0, 1.0);
    } else if (d < 0.01) {
        frag_color = f_color;
    } else if (d < 0.0175) {
        frag_color = vec4(0.0, 0.0, 0.0, 1.0);
    } else {
        discard;
    }
}
