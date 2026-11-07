package de.geolykt.s2dmenues.render.plutovg;

import java.io.Closeable;
import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.nio.charset.StandardCharsets;

import org.jetbrains.annotations.NotNull;
import org.slf4j.LoggerFactory;

import com.badlogic.gdx.utils.Disposable;

public final class PlutoSVGDocument implements AutoCloseable, Closeable, Disposable {

    @NotNull
    private final MemorySegment address;
    private boolean closed;
    @NotNull
    private final Arena dataArena;

    public PlutoSVGDocument(byte[] data, float width, float height) {
        this.dataArena = Arena.ofConfined();

        if (PlutoVGFFI.PLUTOSVG_WITH_DEBUG_DOCUMENT_LOAD) {
            PlutoVGFFI.lastErrorMessage = null;
        }

        try {
            MemorySegment buffer = this.dataArena.allocateFrom(ValueLayout.JAVA_BYTE, data);
            this.address = (@NotNull MemorySegment) PlutoVGFFI.MH_PLUTOSVG_DOCUMENT_LOAD_DATA.invokeExact(buffer, (int) buffer.byteSize(), width, height, MemorySegment.NULL, MemorySegment.NULL);
        } catch (Throwable e) {
            this.dataArena.close();

            throw new RuntimeException("Cannot call MH_PLUTOSVG_DOCUMENT_LOAD_DATA", e);
        }

        if (this.address.address() == 0L) {
            this.dataArena.close();

            if (PlutoVGFFI.PLUTOSVG_WITH_DEBUG_DOCUMENT_LOAD) {
                throw new IllegalStateException("Failed to load SVG document: " + PlutoVGFFI.lastErrorMessage);
            } else {
                throw new NullPointerException("MH_PLUTOSVG_DOCUMENT_LOAD_DATA returned pointer to NULL. Debug information is not available.");
            }
        }
    }

    public PlutoSVGDocument(@NotNull String data, float width, float height) {
        this(data.getBytes(StandardCharsets.UTF_8), width, height);
    }

    private final void checkFreed() {
        if (this.closed) {
            throw new IllegalStateException("use-after-free");
        }
    }

    @Override
    public void close() {
        this.checkFreed();

        try (Arena _ = this.dataArena) {
            PlutoVGFFI.MH_PLUTOSVG_DOCUMENT_DESTROY.invokeExact(this.address);
        } catch (Throwable e) {
            throw new RuntimeException("Cannot call MH_PLUTOSVG_DOCUMENT_DESTROY", e);
        }

        this.closed = true;
    }

    @Override
    public final void dispose() {
        this.close();
    }

    @NotNull
    public PlutoVGSurface render(int width, int height) {
        this.checkFreed();

        if (PlutoVGFFI.PLUTOSVG_WITH_DEBUG_DOCUMENT_RENDER) {
            PlutoVGFFI.lastErrorMessage = "";
        }

        @NotNull
        MemorySegment ret;

        try {
            ret = (@NotNull MemorySegment) PlutoVGFFI.MH_PLUTOSVG_DOCUMENT_RENDER.invokeExact(this.address, MemorySegment.NULL, width, height, MemorySegment.NULL, MemorySegment.NULL, MemorySegment.NULL);
        } catch (Throwable e) {
            throw new RuntimeException("Cannot call MH_PLUTOSVG_DOCUMENT_RENDER", e);
        }

        if (PlutoVGFFI.PLUTOSVG_WITH_DEBUG_DOCUMENT_RENDER) {
            LoggerFactory.getLogger(PlutoSVGDocument.class).info("SVG rendering log: \n{}", PlutoVGFFI.lastErrorMessage);
        }

        if (ret.address() == 0L) {
            throw new NullPointerException("MH_PLUTOSVG_DOCUMENT_RENDER returned pointer to NULL.");
        }

        return new PlutoVGSurface(ret);
    }
}
