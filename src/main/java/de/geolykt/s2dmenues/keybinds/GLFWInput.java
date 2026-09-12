package de.geolykt.s2dmenues.keybinds;

import java.lang.foreign.MemorySegment;

import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFW;

import de.geolykt.starloader.api.event.EventManager;

public class GLFWInput {
    public static void glfwKeyCallback(@NotNull MemorySegment window, int key, int scancode, int action, int mods) {
        KeyEventType type;

        if (action == GLFW.GLFW_RELEASE) {
            type = KeyEventType.RELEASE;
        } else if (action == GLFW.GLFW_PRESS) {
            type = KeyEventType.PRESS;
        } else if (action == GLFW.GLFW_REPEAT) {
            type = KeyEventType.REPEAT;
        } else {
            throw new AssertionError("Unknown action: " + action);
        }

        if (LinuxInput.hasXKB()) {
            EventManager.handleEvent(new XKBGLFWKeyEvent(type, key, scancode, mods));
        } else {
            EventManager.handleEvent(new GLFWKeyEvent(type, key, scancode, mods));
        }
    }
}
