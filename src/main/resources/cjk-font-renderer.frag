uniform vec4 u_foreground;
uniform float u_gamma;
uniform float u_stem_darkening;
in vec2 v_texcoord;
flat in uint v_glyphLoc;
out vec4 fragColor;

void main () {
  float cov;
  vec4 c = hb_gpu_paint (v_texcoord, v_glyphLoc, u_foreground, cov);
  if (cov > 0.0 && cov < 1.0) {
    float adj = cov;
    if (u_stem_darkening > 0.0) {
      float brightness = c.a > 0.0 ? dot (c.rgb, vec3 (1.0 / 3.0)) / c.a : 0.0;
      adj = hb_gpu_stem_darken (adj, brightness, 1.0 / max (fwidth (v_texcoord).x, fwidth (v_texcoord).y));
    }
    if (u_gamma != 1.0)
      adj = pow (adj, u_gamma);
    c *= adj / cov;
  }
  fragColor = c;
}
