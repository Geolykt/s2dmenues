package de.geolykt.s2dmenues.cjk.freetype;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;

import org.jetbrains.annotations.NotNull;
import org.lwjgl.util.freetype.FreeType;

import com.badlogic.gdx.utils.Disposable;

public class FreeTypeLibrary implements AutoCloseable, Disposable {

    @NotNull
    private final Arena arena = Arena.ofConfined();
    @NotNull
    private final MemorySegment libraryPointer;
    private boolean disposed = false;

    public FreeTypeLibrary() {
        this.libraryPointer = this.arena.allocate(ValueLayout.ADDRESS);
        int error = FreeType.nFT_Init_FreeType(this.libraryPointer.address());

        if (error != 0) {
            throw new IllegalStateException("Failed to initialize freetype: " + error);
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

        try {
            int error = FreeType.FT_Done_Library(this.getLibraryAddress());

            if (error != 0) {
                throw new IllegalStateException("Failed to deinitalize freetype: " + error);
            }
        } finally {
            this.arena.close();
        }
    }

    @Override
    public void dispose() {
        this.close();
    }

    public long getLibraryAddress() {
        this.checkFreed();

        long address = this.libraryPointer.get(ValueLayout.ADDRESS, 0).address();

        if (address == 0) {
            throw new NullPointerException();
        }

        return address;
    }
}
