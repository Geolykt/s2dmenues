package de.geolykt.s2dmenues.cjk.harfbuzz;

import java.io.Closeable;
import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;

import org.jetbrains.annotations.NotNull;
import org.lwjgl.util.harfbuzz.HarfBuzz;

import com.badlogic.gdx.utils.Disposable;

import de.geolykt.s2dmenues.cjk.freetype.FreeTypeFace;

public class HarfbuzzFont implements AutoCloseable, Closeable, Disposable {

    private final long address;
    private boolean disposed = false;

    public HarfbuzzFont(@NotNull HarfBuzzFace face) {
        this.address = HarfBuzz.hb_font_create(face.getAddress());

        if (this.address == 0) {
            throw new IllegalStateException("Unable to create font from face: Unknown cause.");
        }
    }

    public HarfbuzzFont(@NotNull FreeTypeFace ftFace) {
        this.address = HarfBuzz.hb_ft_font_create_referenced(ftFace.getFaceAddress());

        if (this.address == 0) {
            throw new IllegalStateException("Unable to create font from freetype face: Unknown cause.");
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
        HarfBuzz.hb_font_destroy(this.address);
    }

    public int @NotNull[] getScale() {
        this.checkFreed();

        try (Arena arena = Arena.ofConfined()) {
            MemorySegment heapSegment = arena.allocate(ValueLayout.JAVA_INT, 2);
            HarfBuzz.nhb_font_get_scale(this.address, heapSegment.address(), heapSegment.address() + ValueLayout.JAVA_INT.byteSize());

            return heapSegment.toArray(ValueLayout.JAVA_INT);
        }
    }

    public void useFreetypeRendering() {
        this.checkFreed();
        HarfBuzz.hb_ft_font_set_funcs(this.address);
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
