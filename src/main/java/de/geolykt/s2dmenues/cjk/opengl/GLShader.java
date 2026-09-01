package de.geolykt.s2dmenues.cjk.opengl;

import java.io.Closeable;

import org.jetbrains.annotations.NotNull;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;
import org.stianloader.micromixin.transform.internal.util.Objects;

import com.badlogic.gdx.utils.Disposable;

public class GLShader implements AutoCloseable, Closeable, Disposable {
    public static enum ShaderType {
        FRAGMENT(GL20.GL_FRAGMENT_SHADER),
        VERTEX(GL20.GL_VERTEX_SHADER);

        private final int cValue;

        private ShaderType(int cValue) {
            this.cValue = cValue;
        }
    }

    private boolean compiled;
    private boolean disposed;
    private final int glShaderId;

    public GLShader(@NotNull ShaderType type) {
        this.glShaderId = GL20.glCreateShader(type.cValue);
        GLCommon.checkGLError();
    }

    public void checkCompiled() {
        this.checkFreed();

        if (!this.compiled) {
            throw new IllegalStateException("This shader has not yet been compiled.");
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
        GL20.glDeleteShader(this.glShaderId);
    }

    public void compile() {
        this.checkFreed();
        GL20.glCompileShader(this.glShaderId);

        int status = GL20.glGetShaderi(this.glShaderId, GL20.GL_COMPILE_STATUS);

        if (status == GL11.GL_FALSE) {
            throw new IllegalStateException("Failed to compile shader (GL error " + GLCommon.getErrorString(GL11.glGetError()) + "). Shader log:\n" + this.getLog().indent(2));
        } else {
            GLCommon.checkGLError();
            this.compiled = true;
        }
    }

    @Override
    public void dispose() {
        this.close();
    }

    public int getGLShaderId() {
        this.checkFreed();
        return this.glShaderId;
    }

    @NotNull
    public String getLog() {
        this.checkFreed();
        return Objects.requireNonNull(GL20.glGetShaderInfoLog(this.glShaderId));
    }

    public void withSources(@NotNull CharSequence @NotNull... sources) {
        this.checkFreed();
        GL20.glShaderSource(this.glShaderId, Objects.requireNonNull(sources, "'sources' may not be null"));
        GLCommon.checkGLError();
    }
}
