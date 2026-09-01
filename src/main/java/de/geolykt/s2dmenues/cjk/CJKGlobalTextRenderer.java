package de.geolykt.s2dmenues.cjk;

import java.io.Closeable;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.concurrent.NotThreadSafe;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.opengl.GL11;
import org.slf4j.LoggerFactory;
import org.stianloader.micromixin.transform.internal.util.Objects;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.utils.Disposable;

import de.geolykt.s2dmenues.S2DI18N;
import de.geolykt.s2dmenues.cjk.harfbuzz.HarfBuzzBuffer;
import de.geolykt.s2dmenues.cjk.harfbuzz.HarfBuzzFace;
import de.geolykt.s2dmenues.cjk.harfbuzz.HarfBuzzLanguage;
import de.geolykt.s2dmenues.cjk.harfbuzz.HarfbuzzBlob;
import de.geolykt.s2dmenues.cjk.harfbuzz.HarfbuzzFont;
import de.geolykt.s2dmenues.cjk.harfbuzz.S2DHarfBuzzGPUState;
import de.geolykt.s2dmenues.cjk.harfbuzz.S2DHarfBuzzGPUState.GlyphCacheInfo;
import de.geolykt.s2dmenues.cjk.harfbuzz.ShapedGlyph;
import de.geolykt.s2dmenues.cjk.opengl.S2DGlyphVertexBuffer;

@NotThreadSafe
public class CJKGlobalTextRenderer implements Disposable, Closeable {

    public static record RenderedGlyphInfo(@NotNull ShapedGlyph shaped, int relX, int relY, @NotNull GlyphCacheInfo extents) { }

    @Nullable
    private static CJKGlobalTextRenderer regular;

    @NotNull
    public static CJKGlobalTextRenderer getRegularRenderer() {
        CJKGlobalTextRenderer renderer = CJKGlobalTextRenderer.regular;

        if (renderer != null) {
            return renderer;
        }

        try {
            Path p = Paths.get("MapleMono-NF-CN-Regular.ttf");

            if (Files.notExists(p)) {
                LoggerFactory.getLogger(CJKGlobalTextRenderer.class).info("Resource at path '{}' does not exist. Downloading it over the internet.", p);

                URI uri = URI.create("https://stianloader.org/public-files/MapleMono-NF-CN-Regular.ttf");

                try (HttpClient client = HttpClient.newHttpClient()) {
                    client.send(HttpRequest.newBuilder(uri).GET().build(), BodyHandlers.ofFile(p)).body();
                } catch (InterruptedException e) {
                    throw new IOException("Cannot download resource from the internet: " + uri.toASCIIString());
                }
            }

            return CJKGlobalTextRenderer.regular = new CJKGlobalTextRenderer(Files.readAllBytes(p));
        } catch (IOException e) {
            throw new UncheckedIOException("Cannot initialize text renderer", e);
        }
    }

    private boolean disposed = false;
    @NotNull
    private final S2DHarfBuzzGPUState gpuState;
    @NotNull
    private final HarfBuzzFace hbFace;
    @NotNull
    private final HarfbuzzFont hbFont;

    public CJKGlobalTextRenderer(byte @NotNull[] resource) {
        try (TWRAutoCloseablePassthrough resourcemngt = new TWRAutoCloseablePassthrough();
                HarfbuzzBlob blobFontData = new HarfbuzzBlob(resource)) {

            resourcemngt.addResource(this.hbFace = new HarfBuzzFace(blobFontData, 0));
            resourcemngt.addResource(this.hbFont = new HarfbuzzFont(this.hbFace));
            resourcemngt.addResource(this.gpuState = new S2DHarfBuzzGPUState("cjk-font-renderer"));

            resourcemngt.disablePassthrough();
        }
    }

    protected final void checkFreed() {
        if (this.disposed) {
            throw new IllegalStateException("use-after-free");
        }
    }

    @Override
    public void close() {
        this.dispose();
    }

    @Override
    public void dispose() {
        this.checkFreed();
        this.disposed = true;

        try (var _ = this.hbFont;
                var _ = this.hbFace;
                var _ = this.gpuState) {
            // Close enclosed resources
        }
    }

    public int[] getFontScale() {
        this.checkFreed();

        return this.hbFont.getScale();
    }

    public final boolean isClosed() {
        return this.isDisposed();
    }

    public boolean isDisposed() {
        return this.disposed;
    }

    /**
     * Draws a single line of LTR text. This method does not do text wrapping. Nor does it handle RTL
     * or BIDI text. It also doesn't handle non-latin scripts very well.
     *
     * @param batch The batch to copy attributes like projection matrices from.
     * @param text The text to render.
     * @param x The x position of the lower left corner of the text to render in world coordinates.
     * @param y The y position of the lower left corner of the text to render in world coordinates.
     * @param fontScale The scale of the font, keep in mind that it should be based on {@link CJKGlobalTextRenderer#getFontScale()}, or rather said a multiple of it's inverse.
     * @param color The color of the rendered text.
     * @return The list of all glyphs that were rendered.
     */
    @NotNull
    public List<@NotNull RenderedGlyphInfo> render(@NotNull Batch batch, @NotNull String text, float x, float y, float fontScale, @NotNull Color color) {
        this.checkFreed();

        @NotNull ShapedGlyph[] glyphsShaped;

        try (HarfBuzzBuffer hbBuffer = new HarfBuzzBuffer()) {
            hbBuffer.addText(text);
            hbBuffer.setLanguage(new HarfBuzzLanguage(S2DI18N.getActiveLocale()));
            hbBuffer.withGuessedSegmentProperties();

            glyphsShaped = hbBuffer.shape(this.hbFont);
        }

        List<@NotNull RenderedGlyphInfo> glyphOut = new ArrayList<>();

        int relX = 0;
        int relY = 0;

        try (Arena arena = Arena.ofConfined()) {
            int vertexIdx = 0;
            MemorySegment vertexData = arena.allocate(S2DGlyphVertexBuffer.GLYPH_VERTEX_LENGTH * 6 * glyphsShaped.length, S2DGlyphVertexBuffer.GLYPH_VERTEX_LAYOUT.byteAlignment());

            for (int i = 0; i < glyphsShaped.length; i++) {
                ShapedGlyph rawGlyph = glyphsShaped[i];

                int relXOff = relX + rawGlyph.xOffset();
                int relYOff = relY + rawGlyph.yOffset();
                float renderX = x + relXOff * fontScale;
                float renderY = y + relYOff * fontScale;

                @NotNull
                GlyphCacheInfo glyph = this.gpuState.lookupGlyph(this.hbFont, rawGlyph.glyphIndex());
                glyphOut.add(new RenderedGlyphInfo(rawGlyph, relXOff, relYOff, glyph));

                if (!glyph.empty()) {
                    final int textureX1 = glyph.x();
                    final int textureX2 = glyph.x() + glyph.width();
                    final int textureY1 = glyph.y() + glyph.height();
                    final int textureY2 = glyph.y();

                    final float worldX1 = renderX;
                    final float worldX2 = renderX + glyph.width() * fontScale;
                    final float worldY1 = renderY;
                    final float worldY2 = renderY - glyph.height() * fontScale;

                    final float invScale = 1F / fontScale;
                    final int glyphAtlasPos = glyph.atlasOffset();

                    // TODO These normals seem to be quite useful when rendering small text. This is the configuration I liked most,
                    // but that's probably not the intended use of normals.
                    // If you feel like it, look into the ideal method of rendering these normals.
                    final float nX1 = -1F;
                    final float nY1 = 1F;
                    final float nX2 = 1F;
                    final float nY2 = 1F;

                    // Triangle 1 (lower left, lower right, upper left) CCW
                    this.renderVertex(worldX1, worldY1, textureX1, textureY1, nX1, nY1, invScale, glyphAtlasPos, vertexData, vertexIdx++);
                    this.renderVertex(worldX2, worldY1, textureX2, textureY1, nX2, nY1, invScale, glyphAtlasPos, vertexData, vertexIdx++);
                    this.renderVertex(worldX1, worldY2, textureX1, textureY2, nX1, nY2, invScale, glyphAtlasPos, vertexData, vertexIdx++);

                    // Triangle 2 (upper left, lower right, upper right) CCW
                    this.renderVertex(worldX1, worldY2, textureX1, textureY2, nX1, nY2, invScale, glyphAtlasPos, vertexData, vertexIdx++);
                    this.renderVertex(worldX2, worldY1, textureX2, textureY1, nX2, nY1, invScale, glyphAtlasPos, vertexData, vertexIdx++);
                    this.renderVertex(worldX2, worldY2, textureX2, textureY2, nX2, nY2, invScale, glyphAtlasPos, vertexData, vertexIdx++);
                }

                relX += rawGlyph.xAdvance();
                relY += rawGlyph.yAdvance();
            }

            MemorySegment viewportInfo = arena.allocate(ValueLayout.JAVA_INT, 4);
            GL11.nglGetIntegerv(GL11.GL_VIEWPORT, viewportInfo.address());
            float[] projTrans = Objects.requireNonNull(batch.getProjectionMatrix().val);

            this.gpuState.setColor(color.r, color.g, color.b, color.a);
            this.gpuState.bind(projTrans, viewportInfo.get(ValueLayout.JAVA_INT, 8), viewportInfo.get(ValueLayout.JAVA_INT, 12));
            this.gpuState.render(vertexData);
        }

        batch.setShader(batch.getShader()); // Hacky way of resetting the shader and uniforms

        return glyphOut;
    }

    private void renderVertex(float x, float y, int tx, int ty, float nx, float ny, float emPerPos, int atlasIdx, @NotNull MemorySegment out, int outIndex) {
        long offset = S2DGlyphVertexBuffer.GLYPH_VERTEX_LENGTH * outIndex;

        out.set(ValueLayout.JAVA_FLOAT, offset, x);
        out.set(ValueLayout.JAVA_FLOAT, offset + 4, y);
        out.set(ValueLayout.JAVA_FLOAT, offset + 8, tx);
        out.set(ValueLayout.JAVA_FLOAT, offset + 12, ty);
        out.set(ValueLayout.JAVA_FLOAT, offset + 16, nx);
        out.set(ValueLayout.JAVA_FLOAT, offset + 20, ny);
        out.set(ValueLayout.JAVA_FLOAT, offset + 24, emPerPos);
        out.set(ValueLayout.JAVA_INT, offset + 28, atlasIdx);
    }
}
