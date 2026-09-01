package de.geolykt.s2dmenues.cjk.harfbuzz;

import java.io.Closeable;
import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.util.harfbuzz.HarfBuzz;

import com.badlogic.gdx.utils.Disposable;

public class HarfbuzzBlob implements AutoCloseable, Closeable, Disposable {

    public static enum MemoryMode {
        DUPLICATE(HarfBuzz.HB_MEMORY_MODE_DUPLICATE),
        READONLY(HarfBuzz.HB_MEMORY_MODE_READONLY),
        @Deprecated
        READONLY_MAY_MAKE_WRITEABLE(HarfBuzz.HB_MEMORY_MODE_READONLY_MAY_MAKE_WRITABLE),
        WRITEABLE(HarfBuzz.HB_MEMORY_MODE_WRITABLE);

        private final int cEnumValue;

        private MemoryMode(int cEnumValue) {
            this.cEnumValue = cEnumValue;
        }
    }

    private final long address;
    private boolean disposed = false;

    public HarfbuzzBlob(byte @NotNull[] data, int length) {
        if (length > data.length) {
            throw new IndexOutOfBoundsException("length exceeds buffer");
        }

        try (Arena arena = Arena.ofConfined()) {
            MemorySegment nativeSegment = arena.allocateFrom(ValueLayout.JAVA_BYTE, data);
            this.address = HarfBuzz.nhb_blob_create_or_fail(nativeSegment.address(), length, MemoryMode.DUPLICATE.cEnumValue, 0L, 0L);
        }

        if (this.address == 0) {
            throw new IllegalStateException("Unable to allocate blob.");
        }
    }

    public HarfbuzzBlob(byte @NotNull[] data) {
        this(data, data.length);
    }

    protected HarfbuzzBlob(long bufferAddress) {
        this.address = bufferAddress;
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
        HarfBuzz.hb_blob_destroy(this.address);
    }

    @Override
    public void dispose() {
        this.close();
    }

    public long getAddress() {
        this.checkFreed();
        return this.address;
    }

    public byte @Nullable[] getData() {
        this.checkFreed();

        try (Arena arena = Arena.ofConfined()) {
            MemorySegment segment = arena.allocate(ValueLayout.JAVA_INT);

            long address = HarfBuzz.nhb_blob_get_data(this.address, segment.address());

            if (address == 0) {
                return null;
            }

            int length = segment.get(ValueLayout.JAVA_INT, 0L);

            return MemorySegment.ofAddress(address).reinterpret(length).toArray(ValueLayout.JAVA_BYTE);
        }
    }
}
