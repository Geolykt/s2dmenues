#version 330

uniform float u_parentAlpha;
in vec2 v_originoffset;

void main()
{
    vec2 diff = v_originoffset / 2;
    vec4 color = vec4(0.9375, 0.3125, 0.0, (1.0 - sqrt(diff.x * diff.x + diff.y * diff.y) * 0.25) * 0.75);
    color.a = min(max(smoothstep(0.0, 1.0, color.a) - 0.0, 0.0), 1.0) * u_parentAlpha;
    gl_FragColor = color;
}
