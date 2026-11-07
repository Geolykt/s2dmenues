package de.geolykt.s2dmenues.render;

import java.nio.ByteBuffer;

import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.Nullable;
import org.lwjgl.nanovg.NanoSVG;

import de.geolykt.starloader.api.event.lifecycle.AtlasPackingEvent.DirectPixmapBlittingFunction;

public final class NSVGRasterizer implements AutoCloseable {
    public final class NSVGImage implements AutoCloseable {
        private boolean closed;
        @NotNull
        private final org.lwjgl.nanovg.@NotNull NSVGImage struct;

        private NSVGImage(@NotNull org.lwjgl.nanovg.@NotNull NSVGImage struct) {
            if (NSVGRasterizer.this.closed) {
                throw new IllegalStateException("Rasterizer already closed");
            }

            this.struct = struct;
        }

        @Override
        public void close() {
            if (this.closed) {
                throw new IllegalStateException("Image already closed");
            } else if (NSVGRasterizer.this.closed) {
                throw new IllegalStateException("Rasterizer already closed");
            }

            NanoSVG.nsvgDelete(this.struct);
            this.closed = true;
        }

        @NotNull
        public DirectPixmapBlittingFunction createBlitter() {
            if (this.closed) {
                throw new IllegalStateException("Image already closed");
            } else if (NSVGRasterizer.this.closed) {
                throw new IllegalStateException("Rasterizer already closed");
            }

            return (buf, off, w, h, stride) -> {
                if (stride > Integer.MAX_VALUE) {
                    throw new AssertionError("Stride outside of 32-bit bounds");
                } else if (off > Integer.MAX_VALUE) {
                    throw new AssertionError("Offset outside of 32-bit bounds");
                }

                this.rasterize(buf.slice((int) off, buf.limit() - (int) off), w, h, (int) stride);
            };
        }

        public float getHeight() {
            return this.getStruct().height();
        }

        @NotNull
        public org.lwjgl.nanovg.@NotNull NSVGImage getStruct() {
            if (this.closed) {
                throw new IllegalStateException("Image already closed");
            } else if (NSVGRasterizer.this.closed) {
                throw new IllegalStateException("Rasterizer already closed");
            }

            return this.struct;
        }

        public float getWidth() {
            return this.getStruct().width();
        }

        public void rasterize(@NotNull ByteBuffer dst, int w, int h, int stride) {
            if (this.closed) {
                throw new IllegalStateException("Image already closed");
            } else if (NSVGRasterizer.this.closed) {
                throw new IllegalStateException("Rasterizer already closed");
            }

            NanoSVG.nsvgRasterize(NSVGRasterizer.this.addr, this.struct, 0F, 0F, ((float) w) / this.getWidth(), dst, w, h, stride);
        }
    }

    private final long addr;
    private boolean closed;

    public NSVGRasterizer() {
        this.addr = NanoSVG.nsvgCreateRasterizer();

        if (this.addr == 0L) {
            throw new NullPointerException("nsvgCreateRasterizer returned a NULL pointer");
        }
    }

    @Override
    public void close() {
        if (this.closed) {
            throw new IllegalStateException("Rasterizer already closed");
        }

        NanoSVG.nsvgDeleteRasterizer(this.addr);

        this.closed = true;
    }

    @NotNull
    public NSVGImage createImage(@NotNull String svgContent) {
        org.lwjgl.nanovg.@Nullable NSVGImage addr = NanoSVG.nsvgParse(svgContent, "px", 96.0F);

        if (addr == null) {
            throw new NullPointerException("nsvgParse retrurned a NULL pointer.");
        }

        return new NSVGImage(addr);
    }
}
