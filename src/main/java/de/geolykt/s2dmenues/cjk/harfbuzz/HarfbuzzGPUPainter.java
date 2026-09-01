package de.geolykt.s2dmenues.cjk.harfbuzz;

import java.io.Closeable;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.util.harfbuzz.HarfBuzzGPU;
import org.lwjgl.util.harfbuzz.hb_glyph_extents_t;

import com.badlogic.gdx.utils.Disposable;

public class HarfbuzzGPUPainter implements AutoCloseable, Closeable, Disposable {

    protected record EncodedGlyph(int xBearing, int yBearing, int width, int height, byte @Nullable[] encodedData) { }
    private final long address;

    private boolean disposed = false;

    public HarfbuzzGPUPainter() {
        this.address = HarfBuzzGPU.hb_gpu_paint_create_or_fail();

        if (this.address == 0) {
            throw new IllegalStateException("Unable to create painter: Unknown cause.");
        }
    }

    protected final void checkFreed() {
        if (this.disposed) {
            throw new IllegalStateException("use-after-free");
        }
    }

    public void clearInnerState() {
        this.checkFreed();
        HarfBuzzGPU.hb_gpu_paint_clear(this.address);
    }

    @Override
    public void close() {
        this.checkFreed();
        this.disposed = true;
        HarfBuzzGPU.hb_gpu_paint_destroy(this.address);
    }

    @Override
    public void dispose() {
        this.close();
    }

    @NotNull
    protected EncodedGlyph @NotNull[] encodeGlyphs(@NotNull HarfbuzzFont font, int @NotNull ... glyphs) {
        this.checkFreed();

        @NotNull
        EncodedGlyph @NotNull[] data = new @NotNull EncodedGlyph[glyphs.length];

        try (hb_glyph_extents_t extents = hb_glyph_extents_t.calloc()) {
            for (int i = 0; i < glyphs.length; i++) {
                int glyph = glyphs[i];

                HarfBuzzGPU.hb_gpu_paint_clear(this.address);

                HarfBuzzGPU.hb_gpu_paint_glyph(this.address, font.getAddress(), glyph);

                long blobAddress = HarfBuzzGPU.hb_gpu_paint_encode(this.address, extents);

                if (blobAddress == 0) {
                    throw new NullPointerException("Failed to draw glyph: hb_gpu_paint_encode returned NULL. Unsupported feature?");
                }

                @SuppressWarnings("resource") // Blob is recycled
                HarfbuzzBlob blob = new HarfbuzzBlob(blobAddress);

                byte[] blobData = blob.getData();

                data[i] = new EncodedGlyph(extents.x_bearing(), extents.y_bearing(), extents.width(), extents.height(), blobData);

                HarfBuzzGPU.hb_gpu_paint_recycle_blob(this.address, blobAddress);
            }
        }

        return data;
    }

    public long getAddress() {
        this.checkFreed();
        return this.address;
    }

    @NotNull
    @Deprecated // Remove if no need for this method was found
    public HarfbuzzBlob paintGlyph(@NotNull HarfbuzzFont font, int glyph) {
        this.checkFreed();

        if (!HarfBuzzGPU.hb_gpu_draw_glyph_or_fail(this.address, font.getAddress(), glyph)) {
            throw new RuntimeException("Failed to draw glyph: Unknown cause.");
        }

        try (hb_glyph_extents_t extents = hb_glyph_extents_t.calloc()) {
            long blobAddress = HarfBuzzGPU.hb_gpu_paint_encode(this.address, extents);

            if (blobAddress == 0) {
                throw new NullPointerException("Failed to draw glyph: hb_gpu_paint_encode returned NULL. Unsupported feature?");
            }

            return new HarfbuzzBlob(blobAddress);
        }
    }
}
