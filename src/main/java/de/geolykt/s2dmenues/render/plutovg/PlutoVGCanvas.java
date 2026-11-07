package de.geolykt.s2dmenues.render.plutovg;

import java.io.Closeable;
import java.lang.foreign.MemorySegment;

import javax.annotation.Nonnegative;

import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.NonNull;

import com.badlogic.gdx.utils.Disposable;

public final class PlutoVGCanvas implements Closeable, AutoCloseable, Disposable {

    @NonNull
    private final MemorySegment address;
    private boolean closed;
    @NonNull
    private final PlutoVGSurface surface;

    public PlutoVGCanvas(@NonNull PlutoVGSurface surface) {
        this.surface = surface;

        this.checkFreed();

        try {
            this.address = (@NonNull MemorySegment) PlutoVGFFI.MH_PLUTOVG_CANVAS_CREATE.invokeExact(surface.getAddress());
        } catch (Throwable t) {
            throw new RuntimeException("Cannot call MH_PLUTOVG_CANVAS_CREATE", t);
        }
    }

    @Contract(pure = true)
    private final void checkFreed() {
        if (this.closed) {
            throw new IllegalStateException("use-after-free");
        }

        this.surface.checkFreed();
    }

    @Override
    public void close() {
        this.checkFreed();

        try {
            PlutoVGFFI.MH_PLUTOVG_CANVAS_DESTROY.invokeExact(this.address);
        } catch (Throwable e) {
            throw new RuntimeException("Cannot call MH_PLUTOVG_CANVAS_DESTROY", e);
        }

        this.closed = true;
    }

    @Override
    public void dispose() {
        this.close();
    }

    public void fill(@NonNull PlutoVGPath path) {
        this.checkFreed();

        try {
            PlutoVGFFI.MH_PLUTOVG_CANVAS_FILL_PATH.invokeExact(this.address, path.getAddress());
        } catch (Throwable t) {
            throw new RuntimeException("Cannot call MH_PLUTOVG_CANVAS_FILL_PATH", t);
        }
    }

    public void fillRect(float x, float y, float w, float h) {
        this.checkFreed();

        try {
            PlutoVGFFI.MH_PLUTOVG_CANVAS_FILL_RECT.invokeExact(this.address, x, y, w, h);
        } catch (Throwable t) {
            throw new RuntimeException("Cannot call MH_PLUTOVG_CANVAS_FILL_RECT", t);
        }
    }

    public void stroke(@NonNull PlutoVGPath path) {
        this.checkFreed();

        try {
            PlutoVGFFI.MH_PLUTOVG_CANVAS_STROKE_PATH.invokeExact(this.address, path.getAddress());
        } catch (Throwable t) {
            throw new RuntimeException("Cannot call MH_PLUTOVG_CANVAS_STROKE_PATH", t);
        }
    }

    public void strokeRect(float x, float y, float w, float h) {
        this.checkFreed();

        try {
            PlutoVGFFI.MH_PLUTOVG_CANVAS_STROKE_RECT.invokeExact(this.address, x, y, w, h);
        } catch (Throwable t) {
            throw new RuntimeException("Cannot call MH_PLUTOVG_CANVAS_STROKE_RECT", t);
        }
    }

    public void setColor(@NonNull PlutoVGColor color) {
        this.checkFreed();

        try {
            PlutoVGFFI.MH_PLUTOVG_CANVAS_SET_COLOR.invokeExact(this.address, color.getAddress());
        } catch (Throwable t) {
            throw new RuntimeException("Cannot call MH_PLUTOVG_CANVAS_SET_COLOR", t);
        }
    }

    public void setOpacity(@Nonnegative float opacity) {
        if (opacity < 0F || opacity > 1F) {
            throw new IllegalArgumentException("opacity outside of bounds: [0F,1F]");
        }

        this.checkFreed();

        try {
            PlutoVGFFI.MH_PLUTOVG_CANVAS_SET_OPACITY.invokeExact(this.address, opacity);
        } catch (Throwable t) {
            throw new RuntimeException("Cannot call MH_PLUTOVG_CANVAS_SET_OPACITY", t);
        }
    }

    public void setMatrix(@NonNull PlutoVGMatrix matrix) {
        this.checkFreed();

        try {
            PlutoVGFFI.MH_PLUTOVG_CANVAS_SET_MATRIX.invokeExact(this.address, matrix.getAddress());
        } catch (Throwable t) {
            throw new RuntimeException("Cannot call MH_PLUTOVG_CANVAS_SET_MATRIX", t);
        }
    }
}
