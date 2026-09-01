package de.geolykt.s2dmenues.cjk.harfbuzz;

import java.io.Closeable;
import java.lang.foreign.MemorySegment;
import java.util.HashMap;
import java.util.Map;

import org.jetbrains.annotations.NotNull;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.harfbuzz.HarfBuzzGPU;
import org.stianloader.micromixin.transform.internal.util.Objects;

import com.badlogic.gdx.utils.Disposable;

import de.geolykt.s2dmenues.S2DMenues;
import de.geolykt.s2dmenues.cjk.TWRAutoCloseablePassthrough;
import de.geolykt.s2dmenues.cjk.harfbuzz.HarfbuzzGPUPainter.EncodedGlyph;
import de.geolykt.s2dmenues.cjk.opengl.GLShader;
import de.geolykt.s2dmenues.cjk.opengl.GLShader.ShaderType;
import de.geolykt.s2dmenues.cjk.opengl.GLShaderProgram;
import de.geolykt.s2dmenues.cjk.opengl.S2DGlyphAtlas;
import de.geolykt.s2dmenues.cjk.opengl.S2DGlyphVertexBuffer;

public class S2DHarfBuzzGPUState implements AutoCloseable, Disposable, Closeable {

    public static record GlyphCacheInfo(int x, int y, int width, int height, int @NotNull [] scale, boolean empty, int atlasOffset) { }

    @NotNull
    private final S2DGlyphAtlas atlas;
    @NotNull
    private Map<Integer, GlyphCacheInfo> cachedGlyphs = new HashMap<>();
    private boolean disposed;
    @NotNull
    private final HarfbuzzGPUPainter painter;
    @NotNull
    private final S2DGlyphVertexBuffer vertexBuffer;

    public S2DHarfBuzzGPUState(@NotNull String shaderName) {
        Objects.requireNonNull(shaderName, "'shaderName' may not be null");

        try (TWRAutoCloseablePassthrough resources = new TWRAutoCloseablePassthrough()) {
            resources.addResource(this.atlas = new S2DGlyphAtlas(1024 * 1024));
            resources.addResource(this.painter = new HarfbuzzGPUPainter());
            GLShaderProgram shader;
            resources.addResource(shader = new GLShaderProgram());
            resources.addResource(this.vertexBuffer = new S2DGlyphVertexBuffer(shader));

            try (GLShader fragment = new GLShader(ShaderType.FRAGMENT); GLShader vertex = new GLShader(ShaderType.VERTEX)) {
                fragment.withSources(
                    "#version 330\n",
                    Objects.requireNonNull(HarfBuzzGPU.hb_gpu_shader_source(HarfBuzzGPU.HB_GPU_SHADER_STAGE_FRAGMENT, HarfBuzzGPU.HB_GPU_SHADER_LANG_GLSL), "hb_gpu_shader_source returned null"),
                    Objects.requireNonNull(HarfBuzzGPU.hb_gpu_paint_shader_source(HarfBuzzGPU.HB_GPU_SHADER_STAGE_FRAGMENT, HarfBuzzGPU.HB_GPU_SHADER_LANG_GLSL), "hb_gpu_paint_shader_source returned null"),
                    S2DMenues.readStringFromResources(shaderName + ".frag")
                );

                vertex.withSources(
                    "#version 330\n",
                    Objects.requireNonNull(HarfBuzzGPU.hb_gpu_shader_source(HarfBuzzGPU.HB_GPU_SHADER_STAGE_VERTEX, HarfBuzzGPU.HB_GPU_SHADER_LANG_GLSL), "hb_gpu_shader_source returned null"),
                    Objects.requireNonNull(HarfBuzzGPU.hb_gpu_paint_shader_source(HarfBuzzGPU.HB_GPU_SHADER_STAGE_VERTEX, HarfBuzzGPU.HB_GPU_SHADER_LANG_GLSL), "hb_gpu_paint_shader_source returned null"),
                    S2DMenues.readStringFromResources(shaderName + ".vert")
                );

                fragment.compile();
                vertex.compile();

                shader.link(fragment, vertex);

                // The underlying shaders may get deleted after linking.
            }

            resources.disablePassthrough();
        }
    }

    public void bind(float @NotNull[] uProjTrans, float viewportWidth, float viewportHeight) {
        this.checkFreed();
        this.vertexBuffer.bind(this.atlas.getBoundTextureUnit(), uProjTrans, viewportWidth, viewportHeight);

        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_ONE, GL11.GL_ONE_MINUS_SRC_ALPHA);
    }

    protected final void checkFreed() {
        if (this.disposed) {
            throw new IllegalStateException("use-after-free");
        }
    }

    @Override
    public void close() {
        this.checkFreed();
        this.disposed = true;

        try (var _ = this.painter; var _ = this.vertexBuffer; var _ = this.atlas) {}
    }

    @Override
    public void dispose() {
        this.close();
    }

    @NotNull
    public GlyphCacheInfo lookupGlyph(@NotNull HarfbuzzFont font, int glyphId) {
        this.checkFreed();

        GlyphCacheInfo ret = this.cachedGlyphs.get(glyphId);

        if (ret != null) {
            return ret;
        }

        int[] scale = font.getScale();

        @NotNull
        EncodedGlyph @NotNull[] encodedData = this.painter.encodeGlyphs(font, glyphId);

        if (encodedData.length != 1) {
            throw new AssertionError();
        }

        EncodedGlyph glyph = encodedData[0];

        boolean empty;
        int glyphPage;
        byte[] glyphPixels = glyph.encodedData();

        if (glyphPixels == null) {
            empty = true;
            glyphPage = -1;
        } else {
            empty = false;
            glyphPage = this.atlas.upload(glyphPixels);
        }

        ret = new GlyphCacheInfo(glyph.xBearing(), glyph.yBearing(), glyph.width(), glyph.height(), scale, empty, glyphPage);

        this.cachedGlyphs.put(glyphId, ret);

        return ret;
    }

    public void render(@NotNull MemorySegment vertexData) {
        this.checkFreed();
        this.vertexBuffer.render(vertexData);
    }

    public void setColor(float r, float g, float b, float a) {
        this.checkFreed();
        this.vertexBuffer.setColor(r, g, b, a);
    }

    public int uploadToAtlas(byte @NotNull [] encodedData) {
        this.checkFreed();
        return this.atlas.upload(encodedData);
    }
}
