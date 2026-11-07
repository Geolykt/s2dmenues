package de.geolykt.s2dmenues.render.plutovg;

import java.io.Closeable;
import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.util.Objects;

import org.jspecify.annotations.NonNull;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Disposable;

public final class PlutoVGPath implements AutoCloseable, Closeable, Disposable {

    @NonNull
    private final MemorySegment address;
    private boolean closed;

    public PlutoVGPath(@NonNull String content) {
        try {
            this.address = (@NonNull MemorySegment) PlutoVGFFI.MH_PLUTOVG_PATH_CREATE.invokeExact();
        } catch (Throwable t) {
            throw new RuntimeException("Cannot call MH_PLUTOVG_SURFACE_CREATE", t);
        }

        if (this.address.address() == 0L) {
            throw new NullPointerException("MH_PLUTOVG_PATH_CREATE returned pointer to NULL.");
        }

        this.parse(content);
    }

    protected final void checkFreed() {
        if (this.closed) {
            throw new IllegalStateException("use-after-free");
        }
    }

    @Override
    public void close() {
        this.checkFreed();

        try {
            PlutoVGFFI.MH_PLUTOVG_PATH_DESTROY.invokeExact(this.address);
        } catch (Throwable e) {
            throw new RuntimeException("Cannot call MH_PLUTOVG_PATH_DESTROY", e);
        }

        this.closed = true;
    }

    @Override
    public final void dispose() {
        this.close();
    }

    @NonNull
    protected final MemorySegment getAddress() {
        return this.address;
    }

    @NonNull
    public final Rectangle getBounds(boolean tight) {
        try (Arena arena = Arena.ofConfined()) {
            MemorySegment boundsOut = arena.allocate(ValueLayout.JAVA_FLOAT, 4);

            float _ = (float) PlutoVGFFI.MH_PLUTOVG_PATH_EXTENTS.invokeExact(this.address, boundsOut, tight);

            return new Rectangle(boundsOut.getAtIndex(ValueLayout.JAVA_FLOAT, 0), boundsOut.getAtIndex(ValueLayout.JAVA_FLOAT, 1), boundsOut.getAtIndex(ValueLayout.JAVA_FLOAT, 2), boundsOut.getAtIndex(ValueLayout.JAVA_FLOAT, 3));
        } catch (Throwable t) {
            throw new RuntimeException("Cannot call MH_PLUTOVG_PATH_EXTENTS", t);
        }
    }

    private final void parse(@NonNull String commands) {
        try (Arena arena = Arena.ofConfined()) {
            MemorySegment cStr = arena.allocateFrom(Objects.requireNonNull(commands, "'commands' may not be null"));

            if (!(boolean) PlutoVGFFI.MH_PLUTOVG_PATH_PARSE.invokeExact(this.address, cStr, (int) (cStr.byteSize() - 1))) {
                throw new IllegalStateException("Cannot parse path: MH_PLUTOVG_PATH_PARSE returned false (unspecified error during path parsing).");
            }
        } catch (Throwable t) {
            if (t instanceof RuntimeException) {
                throw (RuntimeException) t;
            } else {
                throw new RuntimeException("Cannot call MH_PLUTOVG_PATH_PARSE", t);
            }
        }
    }
}
