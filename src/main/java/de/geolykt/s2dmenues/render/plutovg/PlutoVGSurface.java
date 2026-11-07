package de.geolykt.s2dmenues.render.plutovg;

import java.io.Closeable;
import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;

import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.NonNull;

import com.badlogic.gdx.utils.Disposable;

import de.geolykt.starloader.api.event.lifecycle.AtlasPackingEvent.DirectPixmapBlittingFunction;

public final class PlutoVGSurface implements AutoCloseable, Closeable, Disposable {

    private boolean closed;
    @NonNull
    private final MemorySegment dataAddress;
    @NonNull
    private final MemorySegment structAddress;

    public PlutoVGSurface(int width, int height) {
        try {
            this.structAddress = (@NonNull MemorySegment) PlutoVGFFI.MH_PLUTOVG_SURFACE_CREATE.invokeExact(width, height);
        } catch (Throwable e) {
            throw new RuntimeException("Cannot call MH_PLUTOVG_SURFACE_CREATE", e);
        }

        if (this.structAddress.address() == 0L) {
            throw new NullPointerException("MH_PLUTOVG_SURFACE_CREATE returned pointer to NULL.");
        }

        try {
            this.dataAddress = (@NonNull MemorySegment) PlutoVGFFI.MH_PLUTOVG_SURFACE_GET_DATA.invokeExact(this.structAddress);
        } catch (Throwable e) {
            throw new RuntimeException("Cannot call MH_PLUTOVG_SURFACE_GET_DATA", e);
        }

        if (this.dataAddress.address() == 0L) {
            throw new NullPointerException("MH_PLUTOVG_SURFACE_GET_DATA returned pointer to NULL.");
        }
    }

    PlutoVGSurface(@NonNull MemorySegment structAddress) {
        this.structAddress = structAddress;

        try {
            this.dataAddress = (@NonNull MemorySegment) PlutoVGFFI.MH_PLUTOVG_SURFACE_GET_DATA.invokeExact(this.structAddress);
        } catch (Throwable e) {
            throw new RuntimeException("Cannot call MH_PLUTOVG_SURFACE_GET_DATA", e);
        }

        if (this.dataAddress.address() == 0L) {
            throw new NullPointerException("MH_PLUTOVG_SURFACE_GET_DATA returned pointer to NULL.");
        }
    }

    public PlutoVGSurface(@NonNull MemorySegment data, int width, int height, int stride) {
        if (data.address() == 0L) {
            throw new NullPointerException("The argument 'data' is a pointer to NULL.");
        }

        try {
            this.structAddress = (@NonNull MemorySegment) PlutoVGFFI.MH_PLUTOVG_SURFACE_CREATE_FOR_DATA.invokeExact(data, width, height, stride);
        } catch (Throwable e) {
            throw new RuntimeException("Cannot call MH_PLUTOVG_SURFACE_CREATE_FOR_DATA", e);
        }

        if (this.structAddress.address() == 0L) {
            throw new NullPointerException("MH_PLUTOVG_SURFACE_CREATE returned pointer to NULL.");
        }

        this.dataAddress = data;
    }

    @NonNull
    public final DirectPixmapBlittingFunction asBlittingFunction() {
        return (destinationBase, destinationOffset, width, height, destinationStride) -> {
            if (width != this.getWidth() || height != this.getHeight()) {
                throw new IllegalArgumentException("Allocated space for the blitting function does not match the surface's dimensions. Allocated space in destination buffer: " + width + "x" + height + " pixels, surface: " + this.getWidth() + "x" + this.getHeight());
            }

            int sourceStride = this.getStride();
            MemorySegment source = this.dataAddress.asReadOnly().reinterpret(width * height * 4);
            MemorySegment destination = MemorySegment.ofBuffer(destinationBase).asSlice(destinationOffset);

            while (height-- != 0) {
                try {
                    PlutoVGFFI.MH_PLUTOVG_CONVERT_ARGB_TO_RGBA.invokeExact(destination, source, width, 1, 0);
                } catch (Throwable t) {
                    throw new RuntimeException(t);
                }

                source = source.asSlice(sourceStride);
                destination = destination.asSlice(destinationStride);
            }
        };
    }

    @Contract(pure = true)
    protected final void checkFreed() {
        if (this.closed) {
            throw new IllegalStateException("use-after-free");
        }

        if (!this.dataAddress.scope().isAlive()) {
            throw new AssertionError("The underlying scope of the data buffer is no longer alive.");
        }
    }

    @Override
    public void close() {
        if (this.closed) {
            throw new IllegalStateException("Struct already freed");
        }

        try {
            PlutoVGFFI.MH_PLUTOVG_SURFACE_DESTROY.invokeExact(this.structAddress);
        } catch (Throwable e) {
            throw new RuntimeException("Cannot call MH_PLUTOVG_SURFACE_DESTROY", e);
        }

        this.closed = true;

        if (!this.dataAddress.scope().isAlive()) {
            throw new IllegalStateException("The underlying scope of the data buffer is no longer alive whilst attempting to free the struct.");
        }
    }

    public void convertDataToRGBA8888Inplace() {
        this.checkFreed();

        try {
            PlutoVGFFI.MH_PLUTOVG_CONVERT_ARGB_TO_RGBA.invokeExact(this.dataAddress, this.dataAddress, this.getWidth(), this.getHeight(), this.getStride());
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    @Override
    public final void dispose() {
        this.close();
    }

    @Contract(pure = true)
    protected final MemorySegment getAddress() {
        this.checkFreed();

        return this.structAddress;
    }

    /**
     * Returns a pre-multiplied ARGB8888 memory segment describing the <b>address</b> of the start
     * of the surface's buffer.
     *
     * @return A {@link MemorySegment} describing the location of the pixelbuffer for this surface.
     */
    @NonNull
    public final MemorySegment getARGB8888PremulitipliedAddress() {
        this.checkFreed();

        return this.dataAddress;
    }

    /**
     * Returns the height of the underlying pixel buffer obtained through {@link #getARGB8888PremulitipliedAddress()}.
     *
     * @return The surface's height in pixels.
     */
    public final int getHeight() {
        this.checkFreed();

        try {
            return (int) PlutoVGFFI.MH_PLUTOVG_SURFACE_GET_HEIGHT.invokeExact(this.structAddress);
        } catch (Throwable e) {
            throw new RuntimeException("Cannot call MH_PLUTOVG_SURFACE_GET_HEIGHT", e);
        }
    }

    /**
     * Returns the stride of the underlying pixel buffer obtained through {@link #getARGB8888PremulitipliedAddress()}.
     *
     * <p>The stride is the amount of <b>bytes</b> between the start of two lines within a surface.
     *
     * @return The surface's stride within the pixelbuffer obtained through {@link #getARGB8888PremulitipliedAddress()}.
     */
    public final int getStride() {
        this.checkFreed();

        try {
            return (int) PlutoVGFFI.MH_PLUTOVG_SURFACE_GET_STRIDE.invokeExact(this.structAddress);
        } catch (Throwable e) {
            throw new RuntimeException("Cannot call MH_PLUTOVG_SURFACE_GET_STRIDE", e);
        }
    }

    /**
     * Returns the width of the underlying pixel buffer obtained through {@link #getARGB8888PremulitipliedAddress()}.
     *
     * @return The surface's width in pixels.
     */
    public final int getWidth() {
        this.checkFreed();

        try {
            return (int) PlutoVGFFI.MH_PLUTOVG_SURFACE_GET_WIDTH.invokeExact(this.structAddress);
        } catch (Throwable e) {
            throw new RuntimeException("Cannot call MH_PLUTOVG_SURFACE_GET_WIDTH", e);
        }
    }

    public boolean writeToPNG(@NonNull String filename) {
        this.checkFreed();

        try (Arena arena = Arena.ofConfined()) {
            return (boolean) PlutoVGFFI.MH_PLUTOVG_SURFACE_WRITE_TO_PNG.invokeExact(this.structAddress, arena.allocateFrom(filename));
        } catch (Throwable e) {
            throw new RuntimeException("Cannot call MH_PLUTOVG_SURFACE_WRITE_TO_PNG", e);
        }
    }
}
