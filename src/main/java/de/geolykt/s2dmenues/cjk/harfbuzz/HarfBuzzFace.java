package de.geolykt.s2dmenues.cjk.harfbuzz;

import java.io.Closeable;

import javax.annotation.Nonnegative;

import org.jetbrains.annotations.NotNull;
import org.lwjgl.util.harfbuzz.HarfBuzz;

import com.badlogic.gdx.utils.Disposable;

public class HarfBuzzFace implements AutoCloseable, Closeable, Disposable {

    private final long address;
    private boolean disposed = false;

    public HarfBuzzFace(@NotNull HarfbuzzBlob blob, @Nonnegative int index) {
        if (index < 0) {
            throw new IllegalArgumentException("Invalid index: " + index);
        }

        this.address = HarfBuzz.hb_face_create_or_fail(blob.getAddress(), index);

        if (this.address == 0) {
            throw new IllegalStateException("Unable to create face from blob: No face found at the specified index (" + index + ")");
        }
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
        HarfBuzz.hb_face_destroy(this.address);
    }

    @Override
    public void dispose() {
        this.close();
    }

    public long getAddress() {
        this.checkFreed();
        return this.address;
    }
}
