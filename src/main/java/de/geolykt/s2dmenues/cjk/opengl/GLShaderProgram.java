package de.geolykt.s2dmenues.cjk.opengl;

import java.io.Closeable;
import java.util.NoSuchElementException;

import javax.annotation.WillNotClose;

import org.jetbrains.annotations.NotNull;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;
import org.stianloader.micromixin.transform.internal.util.Objects;

import com.badlogic.gdx.utils.Disposable;

public class GLShaderProgram implements AutoCloseable, Closeable, Disposable {
    private boolean disposed;
    private final int programId;

    public GLShaderProgram() {
        this.programId = GL20.glCreateProgram();
        GL20.glLinkProgram(this.programId);
    }

    public void bind() {
        this.checkFreed();
        GL20.glUseProgram(this.programId);
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
        GL20.glDeleteProgram(this.programId);
    }

    @Override
    public void dispose() {
        this.close();
    }

    public int getAttributeLocation(@NotNull String attributeName) {
        this.checkFreed();

        return GL20.glGetAttribLocation(this.programId, Objects.requireNonNull(attributeName, "attributeName"));
    }

    @NotNull
    public String getLog() {
        this.checkFreed();
        return Objects.requireNonNull(GL20.glGetProgramInfoLog(this.programId));
    }

    public void link(@WillNotClose @NotNull GLShader frag, @WillNotClose @NotNull GLShader vert) {
        this.checkFreed();
        frag.checkCompiled();
        vert.checkCompiled();
        GL20.glAttachShader(this.programId, frag.getGLShaderId());
        GLCommon.checkGLError();
        GL20.glAttachShader(this.programId, vert.getGLShaderId());
        GLCommon.checkGLError();
        GL20.glLinkProgram(this.programId);

        if (GL20.glGetProgrami(this.programId,  GL20.GL_LINK_STATUS) == GL11.GL_FALSE) {
            throw new IllegalStateException("Unable to link shaders (GL error " + GLCommon.getErrorString(GL11.glGetError()) + "):\n" + this.getLog().indent(2));
        }

        GLCommon.checkGLError();
    }

    public int requireAttributeLocation(@NotNull String attributeName) {
        int ret = this.getAttributeLocation(attributeName);

        if (ret < 0) {
            throw new NoSuchElementException("The given attribute was not specified when the program was last linked.");
        }

        return ret;
    }

    public void setUniform(@NotNull String uniformName, float value) {
        this.checkFreed();
        GL20.glUniform1f(GL20.glGetUniformLocation(this.programId, Objects.requireNonNull(uniformName, "uniformName")), value);
    }

    public void setUniform(@NotNull String uniformName, float v1, float v2) {
        this.checkFreed();
        GL20.glUniform2f(GL20.glGetUniformLocation(this.programId, Objects.requireNonNull(uniformName, "uniformName")), v1, v2);
    }

    public void setUniform(@NotNull String uniformName, float v1, float v2, float v3, float v4) {
        this.checkFreed();
        GL20.glUniform4f(GL20.glGetUniformLocation(this.programId, Objects.requireNonNull(uniformName, "uniformName")), v1, v2, v3, v4);
    }

    public void setUniform(@NotNull String uniformName, int value) {
        this.checkFreed();
        GL20.glUniform1i(GL20.glGetUniformLocation(this.programId, Objects.requireNonNull(uniformName, "uniformName")), value);
    }

    public void setUniformMatrix4(@NotNull String uniformName, float @NotNull[] value) {
        this.checkFreed();
        GL20.glUniformMatrix4fv(GL20.glGetUniformLocation(this.programId, Objects.requireNonNull(uniformName, "uniformName")), false, value);
    }
}
