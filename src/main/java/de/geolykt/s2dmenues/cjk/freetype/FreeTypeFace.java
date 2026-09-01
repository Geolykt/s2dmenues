package de.geolykt.s2dmenues.cjk.freetype;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;

import org.jetbrains.annotations.NotNull;
import org.lwjgl.util.freetype.FT_Face;
import org.lwjgl.util.freetype.FreeType;
import org.stianloader.micromixin.transform.internal.util.Objects;

import com.badlogic.gdx.utils.Disposable;

public class FreeTypeFace implements AutoCloseable, Disposable {

    @NotNull
    private final Arena arena = Arena.ofConfined();
    @NotNull
    private final MemorySegment buffer;
    private boolean disposed = false;
    @NotNull
    private final FT_Face face;
    @NotNull
    private final MemorySegment facePointer;
    @NotNull
    private final FreeTypeLibrary library;

    public FreeTypeFace(@NotNull FreeTypeLibrary library, byte @NotNull[] data, long faceIndex) {
        this.library = library;
        this.library.checkFreed();
        this.buffer = this.arena.allocateFrom(ValueLayout.JAVA_BYTE, data);
        this.facePointer = this.arena.allocate(FT_Face.SIZEOF, FT_Face.ALIGNOF);

        int error = FreeType.nFT_New_Memory_Face(library.getLibraryAddress(), this.buffer.address(), this.buffer.byteSize(), faceIndex, this.facePointer.address());

        if (error != 0) {
            throw new IllegalStateException("Failed to initialize face: " + error);
        }

        this.face = Objects.requireNonNull(FT_Face.create(this.getFaceAddress()));
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
            this.library.checkFreed();
            int error = FreeType.FT_Done_Face(this.getFace());

            if (error != 0) {
                throw new IllegalStateException("Failed to deinitalize face: " + error);
            }
        } finally {
            this.arena.close();
        }
    }

    @Override
    public void dispose() {
        this.close();
    }

    @NotNull
    public FT_Face getFace() {
        this.checkFreed();
        return this.face;
    }

    public long getFaceAddress() {
        this.checkFreed();

        long address = this.facePointer.address();

        if (address == 0) {
            throw new NullPointerException();
        }

        return address;
    }
}
