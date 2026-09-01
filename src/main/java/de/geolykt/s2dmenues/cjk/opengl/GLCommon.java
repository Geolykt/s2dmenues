package de.geolykt.s2dmenues.cjk.opengl;

import org.jetbrains.annotations.NotNull;
import org.lwjgl.opengl.GL11;

final class GLCommon {
    static void checkGLError() {
        int errorno = GL11.glGetError();

        if (errorno == GL11.GL_NO_ERROR) {
            return;
        } else {
            throw new IllegalStateException("An error was raised in OpenGL space: " + GLCommon.getErrorString(errorno));
        }
    }

    @NotNull
    static String getErrorString(int errorno) {
        if (errorno == GL11.GL_NO_ERROR) {
            return GL11.GL_NO_ERROR + "/GL_NO_ERROR";
        } else if (errorno == GL11.GL_INVALID_ENUM) {
            return GL11.GL_INVALID_ENUM + "/GL_INVALID_ENUM";
        } else if (errorno == GL11.GL_INVALID_VALUE) {
            return GL11.GL_INVALID_VALUE + "/GL_INVALID_VALUE";
        } else {
            return errorno + "/" + Integer.toHexString(errorno);
        }
    }
}
