package de.geolykt.s2dmenues.cjk.opengl;

import java.io.Closeable;
import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;

import org.jetbrains.annotations.NotNull;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL31;

import com.badlogic.gdx.utils.Disposable;

public class S2DGlyphAtlas implements AutoCloseable, Disposable, Closeable {

    public static class AtlasBufferOverflowException extends IndexOutOfBoundsException {

        private static final long serialVersionUID = 6644762184544252978L;

        public AtlasBufferOverflowException(String message) {
            super(message);
        }
    }

    /**
     * Harfbuzz, and thus this class, uses RGBA16I to encode pixel values.
     *
     * <p>This means: 4 channels * 16 bits / pixel = 64 bits / pixel = 8 bytes / pixel
     */
    public static final int BYTES_PER_PIXEL = 8;

    private final int activeTextureUnit;
    private int allocatedPixels;
    private final int capacity;
    private boolean disposed;
    private final int textureBufferId;
    private final int textureId;

    /**
     * Allocate a new {@link S2DGlyphAtlas}.
     *
     * @param capacity The capacity of this atlas, in pixels.
     */
    public S2DGlyphAtlas(int capacity) {
        this.activeTextureUnit = GL11.glGetInteger(GL13.GL_ACTIVE_TEXTURE);
        GLCommon.checkGLError();
        this.textureId = GL11.glGenTextures();
        GLCommon.checkGLError();

        try {
            this.textureBufferId = GL15.glGenBuffers();
            GLCommon.checkGLError();

            try {
                GL15.glBindBuffer(GL31.GL_TEXTURE_BUFFER, this.textureBufferId);
                GLCommon.checkGLError();
                GL15.glBufferData(GL31.GL_TEXTURE_BUFFER, capacity * S2DGlyphAtlas.BYTES_PER_PIXEL, GL15.GL_DYNAMIC_DRAW);
                GLCommon.checkGLError();
                GL11.glBindTexture(GL31.GL_TEXTURE_BUFFER, this.textureId);
                GLCommon.checkGLError();
                GL31.glTexBuffer(GL31.GL_TEXTURE_BUFFER, GL30.GL_RGBA16I, this.textureBufferId);
                GLCommon.checkGLError();
            } catch (Throwable t) {
                GL15.glDeleteBuffers(this.textureBufferId);

                if (t instanceof RuntimeException) {
                    throw (RuntimeException) t;
                } else if (t instanceof Exception) {
                    throw new RuntimeException(t);
                } else {
                    throw t;
                }
            }
        } catch (Throwable t) {
            GL11.glDeleteTextures(this.textureId);

            if (t instanceof Exception) {
                if (t instanceof RuntimeException) {
                    throw (RuntimeException) t;
                }

                throw new RuntimeException(t);
            } else {
                throw t;
            }
        }

        this.allocatedPixels = 0;
        this.capacity = capacity;
    }

    public void bind() {
        this.checkFreed();
        GL13.glActiveTexture(this.activeTextureUnit);
        GLCommon.checkGLError();
        GL11.glBindTexture(GL31.GL_TEXTURE_BUFFER, this.textureId);
        GLCommon.checkGLError();
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

        GL15.glDeleteBuffers(this.textureBufferId);
        GL11.glDeleteTextures(this.textureId);
        GLCommon.checkGLError();
    }

    @Override
    public void dispose() {
        this.close();
    }

    public int getBoundTextureUnit() {
        this.checkFreed();

        return this.activeTextureUnit;
    }

    public int upload(byte @NotNull[] data) {
        this.checkFreed();

        int pixelCount = data.length / S2DGlyphAtlas.BYTES_PER_PIXEL;

        if (this.allocatedPixels + pixelCount > this.capacity) {
            throw new AtlasBufferOverflowException("Atlas with " + this.capacity + " pixels of capacity overflowed; " + this.allocatedPixels + " pixels have already been allocated, and thus " + (this.capacity - this.allocatedPixels) + " pixels remain. However, uploading this glyph would require " + pixelCount + " pixels of free space to be available within the atlas.");
        }

        int atlasOffset = this.allocatedPixels;
        this.allocatedPixels += pixelCount;

        GL15.glBindBuffer(GL31.GL_TEXTURE_BUFFER, this.textureBufferId);
        GLCommon.checkGLError();

        try (Arena arena = Arena.ofConfined()) {
            MemorySegment segment = arena.allocateFrom(ValueLayout.JAVA_BYTE, data);
            GL15.nglBufferSubData(GL31.GL_TEXTURE_BUFFER, atlasOffset * (long) S2DGlyphAtlas.BYTES_PER_PIXEL, (long) data.length, segment.address());
        }

        GLCommon.checkGLError();

        return atlasOffset;
    }
}
