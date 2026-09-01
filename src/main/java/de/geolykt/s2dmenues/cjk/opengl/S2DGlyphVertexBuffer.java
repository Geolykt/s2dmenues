package de.geolykt.s2dmenues.cjk.opengl;

import java.io.Closeable;
import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemoryLayout.PathElement;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.StructLayout;
import java.lang.foreign.ValueLayout;

import javax.annotation.WillCloseWhenClosed;

import org.jetbrains.annotations.NotNull;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

import com.badlogic.gdx.utils.Disposable;

public class S2DGlyphVertexBuffer implements AutoCloseable, Closeable, Disposable {

    @NotNull
    public static final StructLayout GLYPH_VERTEX_LAYOUT = MemoryLayout.structLayout(
        ValueLayout.JAVA_FLOAT.withName("x"), // Coordinate in world space
        ValueLayout.JAVA_FLOAT.withName("y"),
        ValueLayout.JAVA_FLOAT.withName("tx"), // Coordinate in texture space
        ValueLayout.JAVA_FLOAT.withName("ty"),
        ValueLayout.JAVA_FLOAT.withName("nx"), // Normals
        ValueLayout.JAVA_FLOAT.withName("ny"),
        ValueLayout.JAVA_FLOAT.withName("emPerPos"), // Scale (reciprocal)
        ValueLayout.JAVA_INT.withName("atlasOffset") // Offset in atlas (offset is in pixels)
    ).withName("s2d_glyph_vertex_t");

    public static final long GLYPH_VERTEX_LENGTH = S2DGlyphVertexBuffer.GLYPH_VERTEX_LAYOUT.byteSize();

    private final int bufferId;
    private float colorA = 1F;
    private float colorB;
    private float colorG;
    private float colorR;
    private boolean disposed;
    @NotNull
    @WillCloseWhenClosed
    private final GLShaderProgram shader;
    private final int vaoId;

    public S2DGlyphVertexBuffer(@WillCloseWhenClosed @NotNull GLShaderProgram shader) {
        this.shader = shader;

        try {
            this.vaoId = GL30.glGenVertexArrays();
            GLCommon.checkGLError();
        } catch (Throwable t) {
            this.shader.close();

            if (t instanceof Exception && !(t instanceof RuntimeException)) {
                throw new RuntimeException(t);
            } else {
                throw t;
            }
        }

        try {
            this.bufferId = GL15.glGenBuffers();
            GLCommon.checkGLError();
        } catch (Throwable t) {
            try (var _ = this.shader) {
                GL30.glDeleteVertexArrays(this.vaoId);
                GLCommon.checkGLError();
            }

            if (t instanceof Exception && !(t instanceof RuntimeException)) {
                throw new RuntimeException(t);
            } else {
                throw t;
            }
        }
    }

    public void bind(int atlasTextureUnit, float @NotNull[] uProjTrans, float viewportWidth, float viewportHeight) {
        this.checkFreed();
        this.shader.checkFreed();

        GL30.glBindVertexArray(this.vaoId);
        GLCommon.checkGLError();
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, this.bufferId);
        GLCommon.checkGLError();

        this.shader.bind();
        this.shader.setUniform("hb_gpu_atlas", atlasTextureUnit - GL13.GL_TEXTURE0);
        this.shader.setUniform("u_gamma", 0.1F);
        this.shader.setUniform("u_foreground", this.colorR, this.colorG, this.colorB, this.colorA);
        this.shader.setUniformMatrix4("u_projTrans", uProjTrans);
        this.shader.setUniform("u_viewport", viewportWidth, viewportHeight);

        this.setupAttributeFloat("a_position", "x", 2);
        this.setupAttributeFloat("a_texcoord", "tx", 2);
        this.setupAttributeFloat("a_normal", "nx", 2);
        this.setupAttributeFloat("a_emPerPos", "emPerPos", 1);
        this.setupAttributeUnsignedInt("a_glyphLoc", "atlasOffset", 1);
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

        try (var _ = this.shader) {
            GL15.glDeleteBuffers(this.bufferId);
            GLCommon.checkGLError();
        } finally {
            GL30.glDeleteVertexArrays(this.vaoId);
            GLCommon.checkGLError();
        }
    }

    @Override
    public void dispose() {
        this.close();
    }

    public void render(@NotNull MemorySegment vertexData) {
        this.checkFreed();

        // Verify size and alignment of the underlying memory segment
        final int vertexCount = (int) (vertexData.byteSize() / S2DGlyphVertexBuffer.GLYPH_VERTEX_LENGTH);

        if (vertexData.byteSize() % S2DGlyphVertexBuffer.GLYPH_VERTEX_LENGTH != 0) {
            throw new IllegalArgumentException("The given memory segment has an invalid size (" + vertexData.byteSize() + " bytes); It must be a multiple of the size of s2d_glyph_vertex_t (" + S2DGlyphVertexBuffer.GLYPH_VERTEX_LENGTH + " bytes).");
        } else if (vertexData.address() % S2DGlyphVertexBuffer.GLYPH_VERTEX_LAYOUT.byteAlignment() != 0) {
            String addressHex = Long.toHexString(vertexData.address());
            addressHex += "0".repeat(16 - addressHex.length());

            throw new IllegalArgumentException("The given memory segment (located at address 0x" + addressHex + ") has an invalid alignment. It should share the alignment of s2d_glyph_vertex_t (aligned at " + S2DGlyphVertexBuffer.GLYPH_VERTEX_LAYOUT.byteAlignment() + ")");
        } else if (vertexCount % 6 != 0) {
            throw new IllegalArgumentException(vertexCount + " vertices have been submitted, but expected a multiple of 6 (3 vertices per triangle, 2 triangles per quad).");
        }

        GL15.nglBufferData(GL15.GL_ARRAY_BUFFER, vertexData.byteSize(), vertexData.address(), GL15.GL_DYNAMIC_DRAW); // Could also be GL_STATIC_DRAW
        GLCommon.checkGLError();
        GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, vertexCount);
    }

    public void setColor(float r, float g, float b, float a) {
        this.checkFreed();

        this.colorR = r;
        this.colorG = g;
        this.colorB = b;
        this.colorA = a;
    }

    private void setupAttributeFloat(@NotNull String attributeName, @NotNull String componentName, int size) {
        int position = this.shader.requireAttributeLocation(attributeName);

        GL20.glEnableVertexAttribArray(position);
        GLCommon.checkGLError();
        int stride = (int) S2DGlyphVertexBuffer.GLYPH_VERTEX_LENGTH;
        long offsetBytes = S2DGlyphVertexBuffer.GLYPH_VERTEX_LAYOUT.byteOffset(PathElement.groupElement(componentName));
        GL20.glVertexAttribPointer(position, size, GL11.GL_FLOAT, false, stride, offsetBytes);
        GLCommon.checkGLError();
    }

    private void setupAttributeUnsignedInt(@NotNull String attributeName, @NotNull String componentName, int size) {
        int position = this.shader.requireAttributeLocation(attributeName);

        GL20.glEnableVertexAttribArray(position);
        int stride = (int) S2DGlyphVertexBuffer.GLYPH_VERTEX_LENGTH;
        long offsetBytes = S2DGlyphVertexBuffer.GLYPH_VERTEX_LAYOUT.byteOffset(PathElement.groupElement(componentName));
        GL30.glVertexAttribIPointer(position, size, GL11.GL_UNSIGNED_INT, stride, offsetBytes);
    }
}
